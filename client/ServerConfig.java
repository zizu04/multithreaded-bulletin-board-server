import java.util.List;

public class ServerConfig {
    public final int boardW;
    public final int boardH;
    public final int noteW;
    public final int noteH;
    public final List<String> colors;

    public ServerConfig(int boardW, int boardH, int noteW, int noteH, List<String> colors) {
        this.boardW = boardW;
        this.boardH = boardH;
        this.noteW = noteW;
        this.noteH = noteH;
        this.colors = colors;
    }
}
