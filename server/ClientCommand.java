public class ClientCommand {
    public enum Type {
        POST,
        GET_NOTES,
        GET_PINS,
        PIN,
        UNPIN,
        SHAKE,
        CLEAR,
        DISCONNECT
    }

    public final Type type;

    // used by POST/PIN/UNPIN
    public int x;
    public int y;
    public String color;
    public String message;

    // used by GET_NOTES
    public GetQuery query;

    private ClientCommand(Type type) {
        this.type = type;
    }

    public static ClientCommand post(int x, int y, String color, String message) {
        ClientCommand c = new ClientCommand(Type.POST);
        c.x = x; c.y = y; c.color = color; c.message = message;
        return c;
    }

    public static ClientCommand pin(int x, int y) {
        ClientCommand c = new ClientCommand(Type.PIN);
        c.x = x; c.y = y;
        return c;
    }

    public static ClientCommand unpin(int x, int y) {
        ClientCommand c = new ClientCommand(Type.UNPIN);
        c.x = x; c.y = y;
        return c;
    }

    public static ClientCommand shake() { return new ClientCommand(Type.SHAKE); }
    public static ClientCommand clear() { return new ClientCommand(Type.CLEAR); }
    public static ClientCommand disconnect() { return new ClientCommand(Type.DISCONNECT); }
    public static ClientCommand getPins() { return new ClientCommand(Type.GET_PINS); }

    public static ClientCommand getNotes(GetQuery q) {
        ClientCommand c = new ClientCommand(Type.GET_NOTES);
        c.query = q;
        return c;
    }
}
