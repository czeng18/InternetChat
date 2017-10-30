package client;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Opens connection with a server that is already open and creates a UI for user to interact with.
 * Chats with other users on this port.
 * Connects to default port.
 *
 * @author Caroline Zeng
 * @version 2.0.2
 */

public class Client extends Thread {
    /**
     * Holds host name and port number to connect to
     */
    String[] args;
    /**
     * Connection to host
     */
    Socket socket;
    /**
     * Input stream
     */
    PrintWriter out;
    /**
     * Output stream
     */
    BufferedReader in;

    /**
     * Displays messages
     */
    JTextArea  messageArea;
    /**
     * Input from user
     */
    JTextField textField;

    /**
     * Name input by user
     */
    String name;
    /**
     * IP address or name of server
     */
    String serverAddress;
    /**
     * Server port number
     */
    int portNumber;
    /**
     * Status of connection
     */
    boolean open = true;

    /**
     * Threads that carry out key exchange
     */
    ArrayList<ClientExchangeThread> exchangeThreads = new ArrayList<>();
    /**
     * Private values in key exchange
     */
    ClientKeyExchange[][] ex = new ClientKeyExchange[3][3];
    /**
     * Holds key values for encryption; encrypts and decrypts messages
     */
    Encryptor encryptor;
    /**
     * Keeps Client waiting during key exchange
     */
    Lock lock;

    /**
     * Default constructor for client.
     * Creates placeholder values for the server address and port number.
     */
    public Client()
    {
        // Not sure exactly what this does
        this.setDaemon(false);
        serverAddress = "";
        portNumber    = -1;
        lock = new ReentrantLock();
    }

    /**
     * Constructor for client
     * Allows host to be determined
     * @param args  host information
     */
    public Client(String[] args)
    {
        this.setDaemon(false);
        this.args     = args;
        serverAddress = args[0];
        portNumber    = Integer.parseInt(args[1]);
        lock = new ReentrantLock();

    }

    /**
     * Establish connection to server, and send and receive information from host
     */
    @Override
    public void run()
    {
        try
        {
            // Get info for and establish connection
            if (serverAddress.equals("") || portNumber == -1)
            {
                getServerAddress();
                getPortNumber();
            }
            socket = new Socket(serverAddress, portNumber);
            out    = new PrintWriter(socket.getOutputStream(), true);
            in     = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Set up and do key exchange
            for (int i = 0; i < 3; i++)
            {
                for (int j = 0; j < 3; j++)
                {
                    ex[i][j] = new ClientKeyExchange();
                }
            }
            for (int i = 0; i < 3; i++)
            {
                for (int j = 0; j < 3; j++)
                {
                    ClientExchangeThread t = new ClientExchangeThread(this, i, j);
                    t.start();
                    t.setName("E");
                    exchangeThreads.add(t);
                }
            }

            // Wait for key exchange to finish before continuing
            for (ClientExchangeThread t : exchangeThreads)
            {
                t.join();
            }

            getUserName();

            encryptor = new Encryptor(ex);

            sendMessageA(this.getName() + " has joined");

            try
            {
                String inputLine;
                // Keep receiving information
                while (open)
                {
                    inputLine = in.readLine();

                    // Differentiates information received from name selection process
                    if (!inputLine.equals(null))
                    {
                        // New Client joined Server
                        // Do key exchange again
                        if (inputLine.equals(ClientKeyExchange.KEY))
                        {
                            exchangeThreads.clear();
                            for (int i = 0; i < 3; i++)
                            {
                                for (int j = 0; j < 3; j++)
                                {
                                    ClientExchangeThread u = new ClientExchangeThread(this, i, j);
                                    u.start();
                                    u.setName("F");
                                    exchangeThreads.add(u);
                                }
                            }
                            for (ClientExchangeThread t : exchangeThreads)
                            {
                                t.join();
                            }
                            encryptor = new Encryptor(ex);

                        } else if (inputLine.equals("CLOSED"))
                        {
                            messageArea.append("Server is closed\n");
                            textField.setEditable(false);
                        } else if (!inputLine.equals("NO")) messageArea.append(encryptor.decrypt(inputLine) + "\n");
                    }

                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            out.println("END");
        } catch (UnknownHostException e)
        {
            // Can't connect to host
            JOptionPane.showMessageDialog(null, "Don't know about host " + serverAddress);
            textField.setEditable(false);
        } catch (IOException e)
        {
            // Socket is closed
            JOptionPane.showMessageDialog(null, "Couldn't get I/O for the connection to host " + serverAddress);
            textField.setEditable(false);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Prompt for and save the desired screen name.
     */
    public void getUserName()
    {
        String input;
        try
        {
            name =  JOptionPane.showInputDialog(
                    null,
                    (Object) ("Choose a screen name:"),
                    "Screen name selection",
                    JOptionPane.PLAIN_MESSAGE);

            out.println(name);

            if (name == null)
            {
                System.exit(0);
            } else {
                while ((input = in.readLine()).equals("NO")) {
                    name = JOptionPane.showInputDialog(
                            null,
                            "Choose a screen name:",
                            "Screen name selection",
                            JOptionPane.PLAIN_MESSAGE);
                    out.println(name);
                    if (name == null) System.exit(0);
                }
            }

        } catch (Exception e) {}

        this.setName(name);
        textField.setEditable(true);
    }

    /**
     * Prompt for address of server to connect to
     */
    public void getServerAddress()
    {
        textField.setEditable(false);
        serverAddress = JOptionPane.showInputDialog(
                null,
                "Enter IP Address of the server:",
                "Welcome to the Chatter",
                JOptionPane.QUESTION_MESSAGE);
        if (serverAddress == null) System.exit(0);
    }

    /**
     * Prompt for port number of server to connect to
     */
    public void getPortNumber()
    {
        String input = JOptionPane.showInputDialog(
                null,
                "Enter Port Number of the server:",
                "Welcome to the Chatter",
                JOptionPane.QUESTION_MESSAGE);

        if (input == null) System.exit(0);
        boolean isInt = true;
        try
        {
            portNumber = Integer.parseInt(input);
        } catch (Exception e)
        {
            isInt = false;
            while (!isInt)
            {
                try
                {
                    input = JOptionPane.showInputDialog(
                            null,
                            "Enter Port Number of the server:",
                            "Welcome to the Chatter",
                            JOptionPane.QUESTION_MESSAGE);
                    if (input == null) System.exit(0);
                    portNumber = Integer.parseInt(input);
                    isInt      = true;
                } catch (Exception e1)
                {
                    isInt = false;
                }
            }
        }
    }

    /**
     * Sends encrypted message to server with name of client attached
     * @param s message to send
     */
    public void sendMessage(String s)
    {
        s = getName() + ": " + s;
        s = encryptor.encrypt(s);
        out.println(s);
    }

    /**
     * Sends encrypted message to server without name of client attached
     * @param s message to send
     */
    public void sendMessageA(String s)
    {
        s = encryptor.encrypt(s);
        out.println(s);
    }

    /**
     * Sends encrypted message to server to signal client departure
     */
    public void sendEndMessage()
    {
        sendMessageA(this.getName() + " has left the chat");
    }
}