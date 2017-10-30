package client;

/**
 * Client side of key exchange.
 * Processing methods and information storage for key exchange.
 *
 * @author Caroline Zeng
 * @version 1.0.0
 */

public class ClientKeyExchange {
    /**
     * Private value in key exchange.
     * Received base value will be multiplied to the power of this value.
     */
    int val;
    /**
     * Final key value
     */
    int key;
    /**
     * Current base value in key exchange
     */
    public int trans;
    /**
     * Modulo value in key exchange
     */
    public int mod;

    /**
     * Signal to server to begin key exchange
     */
    public static final String KEY = "KEYEXCHANGE";
    /**
     * Signal from server that current values are the private key
     */
    public static final String DONE = "KEYDONE";
    /**
     * Signal from server that key exchange is not done
     */
    public static final String CONTINUE = "CONTINUE";

    /**
     * Constructor for ClientKeyExchange
     */
    public ClientKeyExchange()
    {
        generatePrivateVal();
    }

    /**
     * Generate a random private value for key exchange
     */
    public void generatePrivateVal()
    {
        val = (int) (Math.random() * 1000) + 10;
    }

    /**
     * Process received information using private value
     * @param inf   received information
     */
    public void processKeyInfo(int[] inf)
    {
        int s = 1, t = inf[0], u = val, n = inf[1];

        // End result: s = (inf[0]^val) % inf[1]
        while (u != 0)
        {
            if (u == 1)
            {
                s = (s * t) % n;
            }
            u--;
            t = (t * t) % n;
        }

        this.mod = inf[1];
        trans    = s;
        inf[0]   = s;
    }

    /**
     * Get resulting information from information processing
     * @return  result of information processing and mod in an int Array
     */
    public int[] getInf()
    {
        return new int[] {trans, mod};
    }

    /**
     * Shift key (result from information processing) into private variable
     */
    public void exDone()
    {
        key   = trans % 97;
        trans = 0;
    }

    protected int getKey()
    {
        return key;
    }
}