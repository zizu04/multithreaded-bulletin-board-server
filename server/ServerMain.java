import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class ServerMain {
    public static void main(String[] args) {
        if (args.length < 6) {
            System.err.println("Usage: java ServerMain <port> <boardW> <boardH> <noteW> <noteH> <color1> ... <colorN>");
            System.exit(1);
        }

        try {
            int port = Integer.parseInt(args[0]);
            int boardW = Integer.parseInt(args[1]);
            int boardH = Integer.parseInt(args[2]);
            int noteW = Integer.parseInt(args[3]);
            int noteH = Integer.parseInt(args[4]);

            if (port <= 0 || boardW <= 0 || boardH <= 0 || noteW <= 0 || noteH <= 0) {
                throw new IllegalArgumentException("All numeric arguments must be positive.");
            }

            String[] colors = Arrays.copyOfRange(args, 5, args.length);
            if (colors.length == 0) {
                throw new IllegalArgumentException("At least one color is required.");
            }

            BoardConfig cfg = new BoardConfig(boardW, boardH, noteW, noteH, colors);
            BoardState state = new BoardState(cfg);

            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Server listening on port " + port);
                System.out.println(cfg);

                while (true) {
                    Socket client = serverSocket.accept();
                    Thread t = new Thread(new ClientHandler(client, state, cfg));
                    t.start();
                }
            }
        } catch (Exception e) {
            System.err.println("Server failed to start: " + e.getMessage());
            System.exit(1);
        }
    }
}
