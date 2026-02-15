import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

public class ClientUI extends JFrame {
    private final NetworkClient net = new NetworkClient();

    private JTextField hostField;
    private JTextField portField;

    private JTextField postXField;
    private JTextField postYField;
    private JComboBox<String> colorBox;
    private JTextArea postMsgArea;

    private JTextField pinXField;
    private JTextField pinYField;

    private JTextField getContainsXField;
    private JTextField getContainsYField;
    private JTextField getRefersToField;
    private JComboBox<String> getColorBox;

    private JTextArea outputArea;

    private JLabel serverInfoLabel;

    public ClientUI() {
        super("CP372 A1 Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);

        net.setOnMessage(msg -> SwingUtilities.invokeLater(() -> {
            outputArea.append(msg + "\n");
        }));

        net.setOnConfig(cfg -> SwingUtilities.invokeLater(() -> {
            serverInfoLabel.setText("Board " + cfg.boardW + "x" + cfg.boardH +
                    " | Note " + cfg.noteW + "x" + cfg.noteH +
                    " | Colors: " + cfg.colors);
            setColors(cfg.colors);
        }));

        setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.add(buildConnectionPanel(), BorderLayout.NORTH);
        top.add(buildServerInfoPanel(), BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        add(buildMainPanel(), BorderLayout.CENTER);
        add(buildOutputPanel(), BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel buildConnectionPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBorder(new TitledBorder("Connection"));

        hostField = new JTextField("127.0.0.1", 12);
        portField = new JTextField("5000", 6);

        JButton connectBtn = new JButton("Connect");
        JButton disconnectBtn = new JButton("Disconnect");

        connectBtn.addActionListener(e -> doConnect());
        disconnectBtn.addActionListener(e -> doDisconnect());

        p.add(new JLabel("Host:"));
        p.add(hostField);
        p.add(new JLabel("Port:"));
        p.add(portField);
        p.add(connectBtn);
        p.add(disconnectBtn);

        return p;
    }

    private JPanel buildServerInfoPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBorder(new TitledBorder("Server Config"));
        serverInfoLabel = new JLabel("Not connected.");
        p.add(serverInfoLabel);
        return p;
    }

    private JPanel buildMainPanel() {
        JPanel main = new JPanel(new GridLayout(1, 3));

        main.add(buildPostPanel());
        main.add(buildGetPanel());
        main.add(buildPinPanel());

        return main;
    }

    private JPanel buildPostPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setBorder(new TitledBorder("POST"));

        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT));
        postXField = new JTextField("0", 5);
        postYField = new JTextField("0", 5);
        colorBox = new JComboBox<>(new String[]{"default"});

        fields.add(new JLabel("x:"));
        fields.add(postXField);
        fields.add(new JLabel("y:"));
        fields.add(postYField);
        fields.add(new JLabel("color:"));
        fields.add(colorBox);

        postMsgArea = new JTextArea(6, 20);
        JScrollPane msgScroll = new JScrollPane(postMsgArea);

        JButton send = new JButton("Send POST");
        send.addActionListener(e -> doPost());

        p.add(fields, BorderLayout.NORTH);
        p.add(msgScroll, BorderLayout.CENTER);
        p.add(send, BorderLayout.SOUTH);

        return p;
    }

    private JPanel buildGetPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setBorder(new TitledBorder("GET"));

        JPanel fields = new JPanel();
        fields.setLayout(new GridLayout(6, 2, 6, 6));

        getColorBox = new JComboBox<>(new String[]{"Any", "default"});
        getContainsXField = new JTextField("", 6);
        getContainsYField = new JTextField("", 6);
        getRefersToField = new JTextField("", 10);

        fields.add(new JLabel("color:"));
        fields.add(getColorBox);
        fields.add(new JLabel("contains x:"));
        fields.add(getContainsXField);
        fields.add(new JLabel("contains y:"));
        fields.add(getContainsYField);
        fields.add(new JLabel("refersTo:"));
        fields.add(getRefersToField);

        JButton getNotesBtn = new JButton("Send GET (notes)");
        getNotesBtn.addActionListener(e -> doGetNotes());

        JButton getPinsBtn = new JButton("Send GET PINS");
        getPinsBtn.addActionListener(e -> net.sendLine(ClientProtocol.buildGetPins()));

        JPanel btns = new JPanel(new GridLayout(2, 1, 6, 6));
        btns.add(getNotesBtn);
        btns.add(getPinsBtn);

        JPanel lower = new JPanel(new BorderLayout());
        lower.add(btns, BorderLayout.NORTH);

        p.add(fields, BorderLayout.NORTH);
        p.add(lower, BorderLayout.CENTER);

        JPanel ops = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton shakeBtn = new JButton("SHAKE");
        JButton clearBtn = new JButton("CLEAR");
        shakeBtn.addActionListener(e -> net.sendLine(ClientProtocol.buildShake()));
        clearBtn.addActionListener(e -> net.sendLine(ClientProtocol.buildClear()));
        ops.add(shakeBtn);
        ops.add(clearBtn);

        p.add(ops, BorderLayout.SOUTH);

        return p;
    }

    private JPanel buildPinPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setBorder(new TitledBorder("PIN / UNPIN / DISCONNECT"));

        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pinXField = new JTextField("0", 5);
        pinYField = new JTextField("0", 5);
        fields.add(new JLabel("x:"));
        fields.add(pinXField);
        fields.add(new JLabel("y:"));
        fields.add(pinYField);

        JButton pinBtn = new JButton("PIN");
        JButton unpinBtn = new JButton("UNPIN");
        pinBtn.addActionListener(e -> doPin());
        unpinBtn.addActionListener(e -> doUnpin());

        JButton disconnectCmdBtn = new JButton("Send DISCONNECT");
        disconnectCmdBtn.addActionListener(e -> {
            net.sendLine(ClientProtocol.buildDisconnect());
            // server will close; we close locally too
            doDisconnect();
        });

        JPanel btns = new JPanel(new GridLayout(3, 1, 6, 6));
        btns.add(pinBtn);
        btns.add(unpinBtn);
        btns.add(disconnectCmdBtn);

        p.add(fields, BorderLayout.NORTH);
        p.add(btns, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildOutputPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new TitledBorder("Output"));

        outputArea = new JTextArea(10, 80);
        outputArea.setEditable(false);
        JScrollPane sp = new JScrollPane(outputArea);

        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private void setColors(List<String> colors) {
        colorBox.removeAllItems();
        for (String c : colors) colorBox.addItem(c);

        getColorBox.removeAllItems();
        getColorBox.addItem("Any");
        for (String c : colors) getColorBox.addItem(c);
    }

    private void doConnect() {
        try {
            String host = hostField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());
            net.connect(host, port);
            outputArea.append("[INFO] Connected.\n");
        } catch (Exception ex) {
            outputArea.append("[ERROR] Connect failed: " + ex.getMessage() + "\n");
        }
    }

    private void doDisconnect() {
        net.disconnect();
        outputArea.append("[INFO] Disconnected.\n");
        serverInfoLabel.setText("Not connected.");
    }

    private void doPost() {
        try {
            int x = Integer.parseInt(postXField.getText().trim());
            int y = Integer.parseInt(postYField.getText().trim());
            String color = (String) colorBox.getSelectedItem();
            String msg = postMsgArea.getText().trim();
            if (msg.isEmpty()) {
                outputArea.append("[ERROR] POST message cannot be empty.\n");
                return;
            }
            net.sendLine(ClientProtocol.buildPost(x, y, color, msg));
        } catch (Exception ex) {
            outputArea.append("[ERROR] POST invalid input: " + ex.getMessage() + "\n");
        }
    }

    private void doPin() {
        try {
            int x = Integer.parseInt(pinXField.getText().trim());
            int y = Integer.parseInt(pinYField.getText().trim());
            net.sendLine(ClientProtocol.buildPin(x, y));
        } catch (Exception ex) {
            outputArea.append("[ERROR] PIN invalid input: " + ex.getMessage() + "\n");
        }
    }

    private void doUnpin() {
        try {
            int x = Integer.parseInt(pinXField.getText().trim());
            int y = Integer.parseInt(pinYField.getText().trim());
            net.sendLine(ClientProtocol.buildUnpin(x, y));
        } catch (Exception ex) {
            outputArea.append("[ERROR] UNPIN invalid input: " + ex.getMessage() + "\n");
        }
    }

    private void doGetNotes() {
        try {
            GetQuery q = new GetQuery();

            String colorSel = (String) getColorBox.getSelectedItem();
            if (colorSel != null && !colorSel.equals("Any")) q.color = colorSel;

            String cx = getContainsXField.getText().trim();
            String cy = getContainsYField.getText().trim();
            if (!cx.isEmpty() || !cy.isEmpty()) {
                if (cx.isEmpty() || cy.isEmpty()) {
                    outputArea.append("[ERROR] contains requires both x and y.\n");
                    return;
                }
                q.containsX = Integer.parseInt(cx);
                q.containsY = Integer.parseInt(cy);
            }

            String ref = getRefersToField.getText().trim();
            if (!ref.isEmpty()) q.refersTo = ref;

            net.sendLine(ClientProtocol.buildGetNotes(q));
        } catch (Exception ex) {
            outputArea.append("[ERROR] GET invalid input: " + ex.getMessage() + "\n");
        }
    }
}
