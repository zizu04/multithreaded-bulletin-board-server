import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientProtocol {

    public static ServerConfig parseConfig(String line) {
        // CONFIG board=<W> <H> note=<w> <h> colors=c1,c2,c3
        // Example: CONFIG board=40 20 note=6 3 colors=white,blue
        if (line == null) throw new IllegalArgumentException("Missing CONFIG line.");
        line = line.trim();
        if (!line.startsWith("CONFIG ")) throw new IllegalArgumentException("Expected CONFIG, got: " + line);

        String rest = line.substring("CONFIG".length()).trim();
        String[] tokens = rest.split("\\s+");

        Integer boardW = null, boardH = null, noteW = null, noteH = null;
        List<String> colors = new ArrayList<>();

        int i = 0;
        while (i < tokens.length) {
            String t = tokens[i];

            if (t.startsWith("board=")) {
                String wStr = t.substring("board=".length());
                if (i + 1 >= tokens.length) throw new IllegalArgumentException("CONFIG board missing height");
                String hStr = tokens[i + 1];
                boardW = Integer.parseInt(wStr);
                boardH = Integer.parseInt(hStr);
                i += 2;
            } else if (t.startsWith("note=")) {
                String wStr = t.substring("note=".length());
                if (i + 1 >= tokens.length) throw new IllegalArgumentException("CONFIG note missing height");
                String hStr = tokens[i + 1];
                noteW = Integer.parseInt(wStr);
                noteH = Integer.parseInt(hStr);
                i += 2;
            } else if (t.startsWith("colors=")) {
                String csv = t.substring("colors=".length());
                if (!csv.isEmpty()) {
                    colors.addAll(Arrays.asList(csv.split(",")));
                }
                i += 1;
            } else {
                throw new IllegalArgumentException("Unknown CONFIG token: " + t);
            }
        }

        if (boardW == null || boardH == null || noteW == null || noteH == null) {
            throw new IllegalArgumentException("CONFIG missing board/note dimensions.");
        }
        if (colors.isEmpty()) colors.add("default");

        return new ServerConfig(boardW, boardH, noteW, noteH, colors);
    }

    public static String buildPost(int x, int y, String color, String msg) {
        return "POST " + x + " " + y + " " + color + " " + msg;
    }

    public static String buildPin(int x, int y) {
        return "PIN " + x + " " + y;
    }

    public static String buildUnpin(int x, int y) {
        return "UNPIN " + x + " " + y;
    }

    public static String buildShake() { return "SHAKE"; }

    public static String buildClear() { return "CLEAR"; }

    public static String buildDisconnect() { return "DISCONNECT"; }

    public static String buildGetPins() { return "GET PINS"; }

    public static String buildGetNotes(GetQuery q) {
        StringBuilder sb = new StringBuilder("GET");
        if (q == null) return "GET";
        if (q.color != null && !q.color.trim().isEmpty()) {
            sb.append(" ").append("color=").append(q.color.trim());
        }
        if (q.hasContains()) {
            sb.append(" ").append("contains=").append(q.containsX).append(" ").append(q.containsY);
        }
        if (q.refersTo != null && !q.refersTo.trim().isEmpty()) {
            sb.append(" ").append("refersTo=").append(q.refersTo.trim());
        }
        return sb.toString();
    }
}
