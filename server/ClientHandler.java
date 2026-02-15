import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final BoardState state;
    private final BoardConfig cfg;

    public ClientHandler(Socket socket, BoardState state, BoardConfig cfg) {
        this.socket = socket;
        this.state = state;
        this.cfg = cfg;
    }

    @Override
    public void run() {
        try (Socket s = socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {

            // Handshake CONFIG line
            String configLine = "CONFIG board=" + cfg.boardW + " " + cfg.boardH +
                    " note=" + cfg.noteW + " " + cfg.noteH +
                    " colors=" + cfg.colorsCsv();
            out.println(configLine);

            String line;
            while ((line = in.readLine()) != null) {
                ServerResponse resp;
                try {
                    ClientCommand cmd = Protocol.parseLine(line);
                    resp = state.handle(cmd);

                    out.println(resp.toWire());

                    if (cmd.type == ClientCommand.Type.DISCONNECT) {
                        break;
                    }
                } catch (ProtocolException pe) {
                    out.println(ServerResponse.error("INVALID_FORMAT", pe.getMessage()).toWire());
                } catch (Exception e) {
                    // Never crash due to client input or unexpected exceptions
                    out.println(ServerResponse.error("SERVER_ERROR", "Internal error.").toWire());
                }
            }

        } catch (IOException e) {
            // Client disconnected abruptly; do nothing
        }
    }
}
