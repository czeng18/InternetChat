package client;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * UI for client end of Chat
 *
 * @author Caroline Zeng
 * @version 2.0.0
 */

public class ClientUI extends JFrame {
    /**
     * Panel to hold reconnection info collection
     */
    JPanel server = new JPanel();
    /**
     * Input area for server for reconnection.
     * Takes IP address or server name
     */
    static JTextField serverName = new JTextField(10);
    /**
     * Input area for server reconnection.
     * Takes port number.
     */
    static JSpinner port = new JSpinner(new SpinnerNumberModel(0, 0, null, 1));
    JLabel serverNameL   = new JLabel("Server Name/IP Address:");
    JLabel portLabel     = new JLabel("Port Number:");
    /**
     * Creates new Client to connect to new server
     */
    JButton connect = new JButton("Connect to server");
    /**
     * Closes connection and ends program
     */
    JButton quit = new JButton("Quit");
    /**
     * Opens dialog box to provide information for user
     */
    JButton help = new JButton("Help");
    /**
     * Messages being sent by this user
     */
    JTextField textField = new JTextField(50);
    /**
     * Messages sent to everyone
     */
    JTextArea messageArea = new JTextArea(8, 40);
    /**
     * Client object for UI.
     * Handles all connection and networking
     */
    static Client client;

    /**
     * ActionListener for server reconnection
     */
    private static abstract class ConnectActionListener implements ActionListener
    {
        JTextField textField;
        JTextArea messageArea;
        /**
         * Constructor for ConnectActionListener
         * @param t JTextField for user input of messages to send
         * @param m JTextArea displaying all messages sent thus far
         */
        public ConnectActionListener(JTextField t, JTextArea m)
        {
            textField   = t;
            messageArea = m;
        }
    }

    /**
     * Constructor for ClientUI
     * @param c client connected to server
     */
    public ClientUI(Client c)
    {
        client = c;
        messageArea.setEditable(false);
        textField.setEditable(false);

        // Add an ActionListener that makes a new client tied to this ClientUI
        // Effectively just connects to a new server
        connect.addActionListener(new ConnectActionListener(textField, messageArea)
        {
            public void actionPerformed(ActionEvent e)
            {
                try {
                    client.sendEndMessage();
                    client.out.println("END");
                } catch (Exception f) {}
                client.open        = false;
                String hostName    = serverName.getText();
                int portNumber     = (int) port.getValue();
                client             = new Client(new String[] {hostName, "" + portNumber});
                client.textField   = textField;
                client.messageArea = messageArea;
                client.start();
                textField.setEditable(true);
            }
        });

        // Add ActionListener to quit program and inform server this connection is closing
        quit.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    client.sendEndMessage();
                    client.out.println("END");
                } catch (Exception n) {}
                System.exit(0);
            }
        });

        // Help message
        help.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String message = "Enter the messages you want to send in the box on the top of the window.";
                message += "\nPress the enter key to send your message.";
                message += "\nTo connect to a new server, enter the IP address or host name in the first text field on the right.";
                message += "\nEnter the port number in the second text field on the right, then click 'Connect to server'.";
                message += "\nYou will be prompted for a screen name. You will be asked for a new screen name until you";
                message += "\nenter a unique screen name for that server. You will then be able to chat with users on that server.";
                message += "\nClick 'Quit' to exit.";
                JOptionPane.showMessageDialog(null, message);
            }
        });

        // Responds to pressing the enter key in the textfield by sending
        // the contents of the text field to the server.
        // Then clear the text area in preparation for the next message.
        textField.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                String s = textField.getText();
                client.sendMessage(s);
                textField.setText("");
            }
        });

        // Organizing JPanels
        JPanel t = new JPanel();
        JPanel s = new JPanel();
        JPanel p = new JPanel();
        t.setLayout(new BoxLayout(t, BoxLayout.X_AXIS));
        s.setLayout(new BoxLayout(s, BoxLayout.X_AXIS));
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        s.setAlignmentX(0);
        p.setAlignmentX(0);

        // Organizing UI layout
        serverName.setMaximumSize(new Dimension(100, 25));
        port.setMaximumSize(new Dimension(100, 25));
        s.add(serverNameL);
        s.add(serverName);
        p.add(portLabel);
        p.add(port);

        server.setLayout(new BoxLayout(server, BoxLayout.Y_AXIS));
        server.setAlignmentX(0);
        server.add(s);
        server.add(p);
        server.add(connect);

        t.add(textField);
        t.add(help);
        t.add(quit);

        DefaultCaret caret = (DefaultCaret)messageArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        getContentPane().add(server, "East");
        getContentPane().add(t, "North");
        getContentPane().add(new JScrollPane(messageArea), "Center");

        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
}