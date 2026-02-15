public class Protocol {

    public static ClientCommand parseLine(String line) throws ProtocolException {
        if (line == null) throw new ProtocolException("Null line.");
        line = line.trim();
        if (line.isEmpty()) throw new ProtocolException("Empty line.");

        // Split only first token for command
        String[] parts = line.split("\\s+");
        String cmd = parts[0];

        switch (cmd) {
            case "POST":
                return parsePost(line);
            case "PIN":
                return parseTwoInt(line, "PIN");
            case "UNPIN":
                return parseTwoInt(line, "UNPIN");
            case "SHAKE":
                if (!line.equals("SHAKE")) throw new ProtocolException("Malformed SHAKE.");
                return ClientCommand.shake();
            case "CLEAR":
                if (!line.equals("CLEAR")) throw new ProtocolException("Malformed CLEAR.");
                return ClientCommand.clear();
            case "DISCONNECT":
                if (!line.equals("DISCONNECT")) throw new ProtocolException("Malformed DISCONNECT.");
                return ClientCommand.disconnect();
            case "GET":
                return parseGet(line);
            default:
                throw new ProtocolException("Unknown command: " + cmd);
        }
    }

    private static ClientCommand parsePost(String line) throws ProtocolException {
        // POST <x> <y> <color> <message...>
        String[] parts = line.split("\\s+", 5);
        if (parts.length < 5) throw new ProtocolException("POST requires: POST x y color message");
        int x = parseInt(parts[1], "x");
        int y = parseInt(parts[2], "y");
        String color = parts[3];
        String message = parts[4];
        if (message.trim().isEmpty()) throw new ProtocolException("POST message cannot be empty.");
        return ClientCommand.post(x, y, color, message);
    }

    private static ClientCommand parseTwoInt(String line, String which) throws ProtocolException {
        String[] parts = line.split("\\s+");
        if (parts.length != 3) throw new ProtocolException(which + " requires: " + which + " x y");
        int x = parseInt(parts[1], "x");
        int y = parseInt(parts[2], "y");
        if (which.equals("PIN")) return ClientCommand.pin(x, y);
        return ClientCommand.unpin(x, y);
    }

    private static ClientCommand parseGet(String line) throws ProtocolException {
        // GET PINS
        // GET [color=<color>] [contains=<x> <y>] [refersTo=<substring>]
        String rest = line.substring(3).trim(); // after "GET"
        if (rest.isEmpty()) {
            // Treat as GET with no filters => all notes
            return ClientCommand.getNotes(new GetQuery());
        }

        if (rest.equals("PINS")) {
            return ClientCommand.getPins();
        }

        GetQuery q = new GetQuery();

        // tokenize but keep refersTo possibly containing '=' or no spaces? spec shows single token substring like "Fred"
        // We'll parse by scanning tokens.
        String[] tokens = rest.split("\\s+");
        int i = 0;
        while (i < tokens.length) {
            String t = tokens[i];

            if (t.startsWith("color=")) {
                q.color = t.substring("color=".length());
                if (q.color.isEmpty()) throw new ProtocolException("color= cannot be empty");
                i++;
            } else if (t.startsWith("contains=")) {
                String after = t.substring("contains=".length());
                if (after.isEmpty()) throw new ProtocolException("contains= missing x");
                int x = parseInt(after, "containsX");
                if (i + 1 >= tokens.length) throw new ProtocolException("contains= requires two ints: contains=x y");
                int y = parseInt(tokens[i + 1], "containsY");
                q.containsX = x;
                q.containsY = y;
                i += 2;
            } else if (t.startsWith("refersTo=")) {
                q.refersTo = t.substring("refersTo=".length());
                if (q.refersTo.isEmpty()) throw new ProtocolException("refersTo= cannot be empty");
                i++;
            } else {
                throw new ProtocolException("Unrecognized GET token: " + t);
            }
        }

        return ClientCommand.getNotes(q);
    }

    private static int parseInt(String s, String name) throws ProtocolException {
        try {
            int v = Integer.parseInt(s);
            if (v < 0) throw new ProtocolException(name + " must be non-negative.");
            return v;
        } catch (NumberFormatException e) {
            throw new ProtocolException("Invalid integer for " + name + ": " + s);
        }
    }
}
