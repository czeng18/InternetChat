package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * server.Server side ExchangeThread.
 * Receives information from Clients for key exchange.
 *
 * @author Caroline Zeng
 * @version 1.0.0
 */

public class ServerExchangeThread extends Thread {
    Socket socket;
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
     * Base and modulo pair values the ExchangeThread carries
     */
    int[] inf;
    /**
     * Status of ExchangeThread
     */
    boolean done = false;
    /**
     * Keeps Socket open while exchange is happening
     */
    Lock lock;

    /**
     * Constructor for ServerExchangeThread
     * @param server    server.Server carrying out key exchange
     * @param s         Socket receiving information from a client ExchangeThread
     */
    public ServerExchangeThread(Server server, Socket s)
    {
        this.server = server;
        socket      = s;

        try
        {
            socket.setKeepAlive(true);
        } catch (SocketException e)
        {
            e.printStackTrace();
        }

        lock = new ReentrantLock();
    }

    /**
     * Open input and output streams, and keep Socket open
     */
    @Override
    public void run()
    {
        try
        {
            in  = new BufferedReader(new InputStreamReader((socket.getInputStream())));
            out = new PrintWriter(socket.getOutputStream(), true);
            lock.lock();
            try
            {
                socket.setKeepAlive(false);
            } catch (SocketException e)
            {
                e.printStackTrace();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Send key exchange information to Client ExchangeThread
     * @param inf   information to send
     */
    public void sendToClient(int[] inf)
    {
        this.out.println(inf[0]);
        this.out.println(inf[1]);
    }

    /**
     * Receive information from Client ExchangeThread
     */
    public void receiveFromClient()
    {
        try
        {
            String inputLine = in.readLine();
            int[] i          = new int[2];
            i[0]             = Integer.parseInt(inputLine);
            inputLine        = in.readLine();
            i[1]             = Integer.parseInt(inputLine);
            this.inf         = i;
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Send information to Client ExchangeThread, and continue exchange
     * @param inf   information to send
     */
    public void cont(int[] inf)
    {
        sendToClient(inf);
        out.println(ServerKeyExchange.CONTINUE);
    }
}