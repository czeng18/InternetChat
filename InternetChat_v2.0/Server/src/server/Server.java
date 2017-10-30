package server;

import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Opens a server port for Clients to connect to for chats
 *
 * @author Caroline Zeng
 * @version 2.0.1
 */

public class Server extends Thread
{
    /**
     * All Handlers associated with Server.
     * Each Handler is associated with a Client connected to the Server.
     */
    ArrayList<Handler> handlers = new ArrayList<Handler>();
    /**
     * ExchangeThreads for key exchange
     */
    ArrayList<ServerExchangeThread> exchange = new ArrayList<>();
    /**
     * Sockets to connect with Client ExchangeThreads for key exchange
     */
    ArrayList<Socket> exchsockets = new ArrayList<>();
    /**
     * Names of all connected Clients
     */
    static ArrayList<String> names = new ArrayList<String>();

    /**
     * Server's port number
     */
    int portNumber;
    /**
     * Status of Server
     */
    boolean open = true;

    /**
     * Socket for connections with Clients.
     * Opens connections.
     */
    ServerSocket serverSocket;
    /**
     * Matrix of public values for key exchange
     */
    ServerKeyExchange[][] ex = new ServerKeyExchange[3][3];

    /**
     * Displays messages of all Clients
     */
    JTextArea messageArea;

    /**
     * Default constructor for server
     */
    public Server() throws IOException
    {
        portNumber   = 4000;
        serverSocket = new ServerSocket(portNumber);
    }

    /**
     * Constructor for server
     * Set portNumber to default port
     * @param messageArea   Component for Handlers to send information to
     */
    public Server(JTextArea messageArea) throws IOException
    {
        portNumber       = 4000;
        this.messageArea = messageArea;
        serverSocket     = new ServerSocket(portNumber);
    }

    /**
     * Constructor for server
     * Give server a Component to put information in and a port
     * @param portNumber    Port the server is connected to
     * @param messageArea   Component for Handlers to send information to
     */
    public Server(int portNumber, JTextArea messageArea) throws IOException
    {
        this.portNumber  = portNumber;
        this.messageArea = messageArea;
        serverSocket     = new ServerSocket(portNumber);

        this.setName("Server" + portNumber);
    }

    /**
     * Send out message to all currently connected Clients
     * @param m Message sent
     */
    public synchronized void sendMessage(String m)
    {
        for (Handler h : handlers)
        {
            h.out.println(m);
        }
        messageArea.append(m + "\n");
    }

    /**
     * Checks whether a name already exists in the list of names of Clients already connected
     * @param name  name to check
     * @return true if not already in list
     *              false if already in list
     */
    public static synchronized boolean addName(String name)
    {
        for (String n : names)
        {
            if (n.equals(name)) return false;
        }
        return true;
    }

    /**
     * Sends out to currently connected Clients that another client has left
     * @param h the Handler associated to the client
     */
    public void clientLeft(Handler h)
    {
        // Trash cleanup
        handlers.remove(h);
        names.remove(h.getName());
    }

    /**
     * Close the server without throwing error to end the program
     */
    public void close()
    {
        for (Handler h : handlers)
        {
            h.out.println("CLOSED");
        }
        try
        {
            serverSocket.close();
        } catch (IOException e)
        {
            System.out.println(e.getStackTrace());
        }
        interrupt();
    }

    /**
     * Carries out key exchange for all Clients with associated ServerKeyExchange
     * @param i x-coordinate of associated ServerKeyExchange
     * @param j y-coordinate of associated ServerKeyExchange
     * @throws IOException
     * @throws InterruptedException
     */
    public void doExchange(int i, int j) throws IOException, InterruptedException
    {
        // Clears leftover stuff from last exchange
        exchange.clear();
        exchsockets.clear();

        // Makes Sockets and ServerExchangeThreads for each Handler/Client connected
        int c = 0;
        for (int k = 0; k < handlers.size(); k++)
        {
            Socket exchangeSocket = serverSocket.accept();
            exchsockets.add(exchangeSocket);

            ServerExchangeThread exch =
                    new ServerExchangeThread(this, exchsockets.get(k));

            exch.setName("E" + c);
            exchange.add(exch);
            c++;
        }

        // Transfer to Array
        ServerExchangeThread[] exchangeThreads = new ServerExchangeThread[exchange.size()];

        for (int k = 0; k < exchange.size(); k++)
        {
            exchangeThreads[k] = exchange.get(k);
        }

        // Create ExchangeTree, start ExchangeThreads, do the key exchange
        ExchangeTreeNode root =
                new ExchangeTreeNode(exchangeThreads);
        ExchangeTree t = new ExchangeTree(root, ex[i][j].inf);

        for (ServerExchangeThread e : exchange)
        {
            e.start();
        }

        this.sleep(10);
        t.doExchange();
    }

    /**
     * Run the server.
     * Receive clients and relegate handling clients to handler threads.
     */
    @Override
    public void run()
    {
        // Generate key exchange public values
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                ex[i][j] = new ServerKeyExchange();
                ex[i][j].start();
            }
        }
        // Wait for all values to be generated
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                try {
                    ex[i][j].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        try
        {
            while (open)
            {
                // Connect with Client
                Socket clientSocket = serverSocket.accept();
                Handler handler     = new Handler(clientSocket, this);

                // Key Exchange
                for (Handler h : handlers)
                {
                    h.out.println(ServerKeyExchange.KEY);
                }

                handler.start();
                handlers.add(handler);

                for (int i = 0; i < 3; i++)
                {
                    for (int j = 0; j < 3; j++)
                    {
                        doExchange(i, j);
                    }
                }

                for (Handler h : handlers)
                {
                    if (h.isInterrupted()) clientLeft(h);
                }
            }
        } catch (IOException e)
        {
            interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}