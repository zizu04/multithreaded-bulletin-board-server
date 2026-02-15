public class ServerResponse {
    public final boolean ok;
    public final String code;     // for ERROR, else null
    public final String message;  // human-readable
    public final String payload;  // optional additional text after OK

    private ServerResponse(boolean ok, String code, String message, String payload) {
        this.ok = ok;
        this.code = code;
        this.message = message;
        this.payload = payload;
    }

    public static ServerResponse ok(String message) {
        return new ServerResponse(true, null, message, null);
    }

    public static ServerResponse ok(String message, String payload) {
        return new ServerResponse(true, null, message, payload);
    }

    public static ServerResponse error(String code, String message) {
        return new ServerResponse(false, code, message, null);
    }

    public String toWire() {
        if (ok) {
            if (payload == null || payload.isEmpty()) return "OK " + message;
            return "OK " + message + " " + payload;
        }
        return "ERROR " + code + " " + message;
    }
}
