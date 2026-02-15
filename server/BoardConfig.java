import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BoardConfig {
    public final int boardW;
    public final int boardH;
    public final int noteW;
    public final int noteH;
    private final Set<String> colors;

    public BoardConfig(int boardW, int boardH, int noteW, int noteH, String[] colors) {
        this.boardW = boardW;
        this.boardH = boardH;
        this.noteW = noteW;
        this.noteH = noteH;

        HashSet<String> tmp = new HashSet<>();
        for (String c : colors) {
            if (c == null || c.trim().isEmpty()) continue;
            tmp.add(c.trim());
        }
        if (tmp.isEmpty()) {
            throw new IllegalArgumentException("Valid colors list cannot be empty.");
        }
        this.colors = Collections.unmodifiableSet(tmp);
    }

    public boolean isValidColor(String c) {
        return c != null && colors.contains(c);
    }

    public Set<String> getColors() {
        return colors;
    }

    public String colorsCsv() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String c : colors) {
            if (!first) sb.append(",");
            sb.append(c);
            first = false;
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "BoardConfig{board=" + boardW + "x" + boardH +
                ", note=" + noteW + "x" + noteH +
                ", colors=" + colors + "}";
    }
}
