package server;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

/**
 * UI for server end of Chat
 *
 * @author Caroline Zeng
 * @version 2.0.0
 */

public class ServerUI extends JFrame {
    /**
     * Input area for server start.
     * Takes port number.
     */
    static JSpinner port = new JSpinner(new SpinnerNumberModel(0, 0, null, 1));
    JLabel portLabel     = new JLabel("Port Number:");
    /**
     * Creates new Client to connect to new server
     */
    JButton connect = new JButton("Connect to server");
    /**
     * Closes program
     */
    JButton quit = new JButton("Quit");
    /**
     * Opens dialog box to provide information for user
     */
    JButton help = new JButton("Help");
    /**
     * Servers hosted on this device using this program
     */
    static ArrayList<ServerViewer> servers = new ArrayList<>();

    /**
     * UI for individual Servers opened on ports on this machine
     */
    private static class ServerViewer extends JFrame
    {
        /**
         * Displays messages for associated server.Server
         */
        JTextArea messageArea = new JTextArea(8, 40);
        /**
         * Closes associated server.Server
         */
        JButton quit = new JButton("Quit");
        JLabel port;
        /**
         * Associated server.Server
         */
        Server server;
        /**
         * Port number of associated server.Server
         */
        int portNumber;
        /**
         * All ServerViewers open on this server.ServerUI
         */
        ArrayList<ServerViewer> servers;

        /**
         * Constructor for ServerViewer
         * @param s             List of ServerViewers that this ServerViewer is contained in
         * @param portNumber    Port the server is connected to
         */
        public ServerViewer(int portNumber, ArrayList<ServerViewer> s)
        {
            this.portNumber = portNumber;
            servers         = s;

            // Open a connection
            try
            {
                server = new Server(portNumber, messageArea);
                server.start();
            } catch (IOException e)
            {
                // If it doesn't work, get rid of this JFrame
                dispose();
            }

            messageArea.setEditable(false);

            // Close this ServerViewer without throwing Exception
            quit.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    quit();
                    dispose();
                }
            });

            // Layout of UI
            port = new JLabel("Port Number: " + portNumber);

            JPanel t = new JPanel();
            t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
            t.add(quit);
            t.add(port);

            DefaultCaret caret = (DefaultCaret) messageArea.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

            getContentPane().add(new JScrollPane(messageArea), "Center");
            getContentPane().add(t, "East");

            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            pack();
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setVisible(true);
        }

        /**
         * Close this ServerViewer and associated server
         */
        public void quit()
        {
            try
            {
                server.close();
                server.open = false;
                servers.remove(this);
            } catch (Exception e) {}
        }
    }

    /**
     * Constructor for server.ServerUI
     */
    public ServerUI()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(portLabel);
        p.add(port);

        // Open a new server on this machine
        // Make sure no other server is open on this port
        connect.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int portNumber        = (int) port.getValue();
                boolean availablePort = true;

                for (ServerViewer v : servers)
                {
                    if (portNumber == v.portNumber) availablePort = false;
                }

                if (availablePort)
                {
                    ServerViewer s = new ServerViewer(portNumber, servers);
                    servers.add(s);
                }
            }
        });

        // Close all Servers hosted on this machine
        quit.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                closeAll();
                System.exit(0);
            }
        });

        help.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String message = "This utility opens ports as servers on your current machine.";
                message += "\nEnter the portnumber you want to open in the text field labelled 'Port Number'.";
                message += "\nClick 'Open server' to open the port.  A window will open showing all messages sent to this server.";
                message += "\nClick 'Quit' in the new window when you want to close the server.";
                message += "\nIt is recommended to open a port number higher than 3000, as many of the lower number ports are used by the computer.";
                message += "\nMultiple servers can be opened on this computer.";
                JOptionPane.showMessageDialog(null, message);
            }
        });

        JPanel b = new JPanel();
        b.setLayout(new BoxLayout(b, BoxLayout.X_AXIS));
        b.add(quit);
        b.add(help);
        b.add(connect);

        getContentPane().add(b, "North");
        getContentPane().add(p, "Center");
        pack();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    /**
     * Close all ServerViewers associated with this machine
     */
    public void closeAll()
    {
        for (ServerViewer s : servers)
        {
            s.quit();
            s.dispose();
        }
    }

    public static void main(String[] args)
    {
        new ServerUI();
    }
}