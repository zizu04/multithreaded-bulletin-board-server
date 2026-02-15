import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class NetworkClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private Consumer<String> onMessage = (s) -> {};
    private Consumer<ServerConfig> onConfig = (c) -> {};

    private Thread readerThread;

    public void setOnMessage(Consumer<String> onMessage) {
        this.onMessage = onMessage != null ? onMessage : (s) -> {};
    }

    public void setOnConfig(Consumer<ServerConfig> onConfig) {
        this.onConfig = onConfig != null ? onConfig : (c) -> {};
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void connect(String host, int port) throws Exception {
        disconnect();

        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Read CONFIG
        String configLine = in.readLine();
        ServerConfig cfg = ClientProtocol.parseConfig(configLine);
        onConfig.accept(cfg);
        onMessage.accept("[SERVER] " + configLine);

        // Start reader thread for subsequent responses
        readerThread = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    onMessage.accept(line);
                }
            } catch (Exception ignored) {
            } finally {
                onMessage.accept("[INFO] Disconnected.");
            }
        });
        readerThread.start();
    }

    public void sendLine(String line) {
        if (!isConnected()) {
            onMessage.accept("[ERROR] Not connected.");
            return;
        }
        out.println(line);
    }

    public void disconnect() {
        try {
            if (out != null) out.flush();
        } catch (Exception ignored) {}

        try {
            if (socket != null) socket.close();
        } catch (Exception ignored) {}

        socket = null;
        in = null;
        out = null;
        readerThread = null;
    }
}
