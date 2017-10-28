package server;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Node in server.ExchangeTree
 *
 * @author Caroline Zeng
 * @version 1.0.0
 */

public class ExchangeTreeNode extends DefaultMutableTreeNode
{
    /**
     * Threads carrying out key exchange
     */
    ServerExchangeThread[] exchangeThreads;
    /**
     * Result from function for key exchange
     */
    int result;
    /**
     * Base and modulo received from associated Client
     */
    int[] info;

    /**
     * Constructor for server.ExchangeTreeNode
     * @param s ExchangeThreads of this node
     */
    public ExchangeTreeNode(ServerExchangeThread[] s)
    {
        exchangeThreads = s;
    }

    /**
     * Process information through Clients associated with node's ExchangeThreads when exchange is not over
     * @param inf   information to process
     */
    public void processSelf(int[] inf)
    {
        int[] temp = inf.clone();

        for (ServerExchangeThread e : exchangeThreads)
        {
            e.cont(temp);
            e.receiveFromClient();
            temp[0] = e.inf[0];
        }

        result = temp[0];
        info   = temp;
    }

    /**
     * Process information through Clients associated with node's ExchangeThreads when exchange is over
     * @param inf   information to process
     */
    public void finish(int[] inf)
    {
        int[] temp = inf.clone();
        int size   = exchangeThreads.length;

        if (size == 2)
        {
            int[][] a = new int[size][2];

            // Process information through each Client the first time
            for (int i = 0; i < size; i++)
            {
                a[i]                   = temp;
                ServerExchangeThread t = exchangeThreads[i];

                t.cont(a[i]);
                t.receiveFromClient();

                a[i] = t.inf;
            }

            // Process information through each Client the last time
            // Finish exchange
            for (int j = 0; j < size; j++)
            {
                ServerExchangeThread t = exchangeThreads[j];
                t.done                 = true;

                t.sendToClient(a[(j + size - 1) % size]);
                t.out.println(ServerKeyExchange.DONE);
            }

        } else if (size == 1)
        {
            ServerExchangeThread t = exchangeThreads[0];
            t.sendToClient(temp);
            t.out.println(ServerKeyExchange.DONE);
        }
    }
}