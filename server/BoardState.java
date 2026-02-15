import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BoardState {
    private final BoardConfig cfg;
    private final List<Note> notes = new ArrayList<>();
    private int nextId = 1;

    private final ReentrantReadWriteLock rw = new ReentrantReadWriteLock(true);

    public BoardState(BoardConfig cfg) {
        this.cfg = cfg;
    }

    public ServerResponse handle(ClientCommand cmd) {
        switch (cmd.type) {
            case POST:
                return post(cmd.x, cmd.y, cmd.color, cmd.message);
            case PIN:
                return pin(cmd.x, cmd.y);
            case UNPIN:
                return unpin(cmd.x, cmd.y);
            case SHAKE:
                return shake();
            case CLEAR:
                return clear();
            case GET_PINS:
                return getPins();
            case GET_NOTES:
                return getNotes(cmd.query);
            case DISCONNECT:
                return ServerResponse.ok("DISCONNECT");
            default:
                return ServerResponse.error("UNKNOWN_COMMAND", "Unsupported command type.");
        }
    }

    private ServerResponse post(int x, int y, String color, String message) {
        rw.writeLock().lock();
        try {
            if (!cfg.isValidColor(color)) {
                return ServerResponse.error("INVALID_COLOR", "Color not allowed: " + color);
            }
            if (!noteInBounds(x, y)) {
                return ServerResponse.error("OUT_OF_BOUNDS", "Note does not fit within board.");
            }

            // complete overlap check: same top-left implies same rectangle due to fixed noteW/noteH
            for (Note n : notes) {
                if (n.x == x && n.y == y) {
                    return ServerResponse.error("OVERLAP_ERROR", "Note completely overlaps existing note id=" + n.id);
                }
            }

            int id = nextId++;
            Note n = new Note(id, x, y, color, message);
            notes.add(n);
            return ServerResponse.ok("POST", "id=" + id);
        } finally {
            rw.writeLock().unlock();
        }
    }

    private ServerResponse pin(int x, int y) {
        rw.writeLock().lock();
        try {
            Point p = new Point(x, y);
            int affected = 0;
            for (Note n : notes) {
                if (n.containsPoint(x, y, cfg)) {
                    n.addPin(p);
                    affected++;
                }
            }
            if (affected == 0) {
                return ServerResponse.error("PIN_MISS", "No note contains point " + p);
            }
            return ServerResponse.ok("PIN", "affected=" + affected);
        } finally {
            rw.writeLock().unlock();
        }
    }

    private ServerResponse unpin(int x, int y) {
        rw.writeLock().lock();
        try {
            Point p = new Point(x, y);
            int affected = 0;
            for (Note n : notes) {
                if (n.removePin(p)) {
                    affected++;
                }
            }
            if (affected == 0) {
                return ServerResponse.error("NO_PIN_AT_POINT", "No pin exists at point " + p);
            }
            return ServerResponse.ok("UNPIN", "affected=" + affected);
        } finally {
            rw.writeLock().unlock();
        }
    }

    private ServerResponse shake() {
        rw.writeLock().lock();
        try {
            int removed = 0;
            Iterator<Note> it = notes.iterator();
            while (it.hasNext()) {
                Note n = it.next();
                if (!n.isPinned()) {
                    it.remove();
                    removed++;
                }
            }
            return ServerResponse.ok("SHAKE", "removed=" + removed);
        } finally {
            rw.writeLock().unlock();
        }
    }

    private ServerResponse clear() {
        rw.writeLock().lock();
        try {
            notes.clear();
            return ServerResponse.ok("CLEAR");
        } finally {
            rw.writeLock().unlock();
        }
    }

    private ServerResponse getPins() {
        rw.readLock().lock();
        try {
            // collect unique pin coordinates across all notes
            java.util.HashSet<Point> set = new java.util.HashSet<>();
            for (Note n : notes) {
                Map<Point, Integer> pins = n.getPinCountsSnapshot();
                set.addAll(pins.keySet());
            }
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Point p : set) {
                if (!first) sb.append(",");
                sb.append(p.toString());
                first = false;
            }
            return ServerResponse.ok("PINS", "count=" + set.size() + " points=" + sb);
        } finally {
            rw.readLock().unlock();
        }
    }

    private ServerResponse getNotes(GetQuery q) {
        rw.readLock().lock();
        try {
            List<Note> out = new ArrayList<>();
            for (Note n : notes) {
                if (q != null) {
                    if (q.color != null && !q.color.equals(n.color)) continue;

                    if (q.hasContains()) {
                        int cx = q.containsX;
                        int cy = q.containsY;
                        if (!n.containsPoint(cx, cy, cfg)) continue;
                    }

                    if (q.refersTo != null && !n.message.contains(q.refersTo)) continue;
                }
                out.add(n);
            }

            // Encode notes into one line:
            // notes=<id>@<x>,<y>,<color>,<pinned>{<msg>};...
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Note n : out) {
                if (!first) sb.append(";");
                sb.append(n.id).append("@")
                  .append(n.x).append(",").append(n.y).append(",")
                  .append(n.color).append(",")
                  .append(n.isPinned() ? "1" : "0")
                  .append("{").append(escapeBraces(n.message)).append("}");
                first = false;
            }
            return ServerResponse.ok("NOTES", "count=" + out.size() + " notes=" + sb);
        } finally {
            rw.readLock().unlock();
        }
    }

    private boolean noteInBounds(int x, int y) {
        // note rectangle must fully fit
        if (x < 0 || y < 0) return false;
        if (x + cfg.noteW > cfg.boardW) return false;
        if (y + cfg.noteH > cfg.boardH) return false;
        return true;
    }

    private String escapeBraces(String s) {
        // very small safety so braces don't break our simple encoding
        return s.replace("{", "\\{").replace("}", "\\}");
    }
}
