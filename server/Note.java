import java.util.HashMap;
import java.util.Map;

public class Note {
    public final int id;
    public final int x;
    public final int y;
    public final String color;
    public final String message;

    // pinCounts allows multiple pins at same point on same note
    private final Map<Point, Integer> pinCounts = new HashMap<>();

    public Note(int id, int x, int y, String color, String message) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.color = color;
        this.message = message;
    }

    public boolean containsPoint(int px, int py, BoardConfig cfg) {
        // Inclusive boundaries policy:
        // Point is inside if x <= px < x+noteW and y <= py < y+noteH
        return px >= x && px < x + cfg.noteW && py >= y && py < y + cfg.noteH;
    }

    public boolean completelyOverlaps(Note other) {
        return this.x == other.x && this.y == other.y;
        // noteW/noteH are fixed globally, so same (x,y) means same rectangle
    }

    public void addPin(Point p) {
        pinCounts.put(p, pinCounts.getOrDefault(p, 0) + 1);
    }

    public boolean removePin(Point p) {
        Integer c = pinCounts.get(p);
        if (c == null || c <= 0) return false;
        if (c == 1) pinCounts.remove(p);
        else pinCounts.put(p, c - 1);
        return true;
    }

    public boolean hasPinAt(Point p) {
        Integer c = pinCounts.get(p);
        return c != null && c > 0;
    }

    public boolean isPinned() {
        return !pinCounts.isEmpty();
    }

    public Map<Point, Integer> getPinCountsSnapshot() {
        return new HashMap<>(pinCounts);
    }
}
