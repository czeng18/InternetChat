package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Thread to handle the information coming from the associated client
 *
 * @author Caroline Zeng
 * @version 2.0.0
 */

public class Handler extends Thread
{
    /**
     * Connection to client
     */
    Socket clientSocket;
    /**
     * Associated server
     */
    Server server;
    /**
     * Read incoming information from client
     */
    BufferedReader in;
    /**
     * Send information to client
     */
    PrintWriter out;

    /**
     * Constructor for server.Handler.
     * Get basic objects needed for connection.
     * @param clientSocket  Connection to client
     * @param s             Associated server
     */
    public Handler(Socket clientSocket, Server s)
    {
        this.clientSocket = clientSocket;
        this.server       = s;
        this.setName("H" + this.getName().substring(this.getName().length()-1));
    }

    /**
     * Run the server.Handler Thread.
     * Receive and send information with the client.
     */
    public void run()
    {
        try {
            // Establish input and output streams
            in  = new BufferedReader(new InputStreamReader((clientSocket.getInputStream())));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            String n = in.readLine();

            if (n != null) {
                // Get name from client, and make sure it does not already exist in the chat
                // Continues to prompt for name until unique name is given
                while (!server.addName(n)) {
                    out.println("NO");
                    n = in.readLine();
                    if (n == null) this.interrupt();
                }
            } else {
                this.interrupt();
            }
            out.println("OK");

            try {
                // Set name of Thread to name from client
                this.setName(n);
                // Add name to existing pool of names
                server.names.add(this.getName());
            } catch (Exception e) {}
            // Inform other users already connected of entrance
            String inputLine;

            // Receive information from user
            // If user has left, inform other users of exit
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.equals("END")) {
                    // Trash cleanup
                    // Get rid of this server.Handler and its name from the server's pool of names and Handlers
                    server.clientLeft(this);
                    break;
                } else {
                    server.sendMessage(inputLine);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}