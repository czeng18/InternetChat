package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Client side ExchangeThread.
 * Carries out key exchange with other clients through server.
 *
 * @author Caroline Zeng
 * @version 1.0.0
 */

public class ClientExchangeThread extends Thread {
    /**
     * Connection to server
     */
    Socket socket;
    /**
     * Output stream
     */
    PrintWriter out;
    /**
     * Input stream
     */
    BufferedReader in;
    /**
     * Client object that ExchangeThread is doing key exchange for
     */
    Client client;
    /**
     * Coordinates of ExchangeThread's private values
     */
    int x, y;

    /**
     * Constructor for ClientExchangeThread
     * @param c Client for which exchange is being carried out for
     * @param i x-coordinate of associated ClientKeyExchange
     * @param j y-coordinate of associated ClientKeyExchange
     */
    public ClientExchangeThread(Client c, int i, int j)
    {
        client = c;
        x = i;
        y = j;
    }

    /**
     * Runs Thread.
     * Carries out exchange, then destroys when done.
     */
    @Override
    public void run()
    {
        // Suspend Client thread until exchange is finished
        client.lock.lock();
        try
        {
            // Open new connection with server specifically for exchange
            socket           = new Socket(client.serverAddress, client.portNumber);
            out              = new PrintWriter(socket.getOutputStream(), true);
            in               = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String inputLine = in.readLine();

            // As long as the server does not signal the exchange as over, keep waiting for new information
            while (inputLine != null &&
                    !inputLine.equals(ClientKeyExchange.DONE))
            {
                // Base and mod for information processing
                int[] i   = new int[2];
                i[0]      = Integer.parseInt(inputLine);
                inputLine = in.readLine();
                i[1]      = Integer.parseInt(inputLine);

                client.ex[x][y].processKeyInfo(i);
                inputLine = in.readLine();

                // Key exchange is not done
                // ExchangeThread sends client's result
                if (inputLine.equals(ClientKeyExchange.CONTINUE))
                {
                    int[] a = client.ex[x][y].getInf();
                    out.println(a[0]);
                    out.println(a[1]);
                    inputLine = in.readLine();
                }
            }

            // Shift key to private variable
            client.ex[x][y].exDone();
            // Allow Client thread to continue
            client.lock.unlock();
            socket.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}