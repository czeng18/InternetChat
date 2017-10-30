package client;

/**
 * Encrypts and decrypts Strings
 *
 * @author Caroline Zeng
 * @version 1.0.0
 */

public class Encryptor extends ClientKeyExchange {
    /**
     * Client private key to encrypt messages
     */
    private float[][] key;
    /**
     * Inverse of private key to decrypt messages
     */
    private float[][] inv;
    Matrices mat;

    /**
     * Constructor for encryptor.
     * Pulls key values from matrix (2-dimensional Array) of ClientKeyExchanges.
     * @param ex    matrix of ClientKeyExchanges
     */
    public Encryptor(ClientKeyExchange[][] ex)
    {
        mat = new Matrices();
        key = new float[3][3];
        getKey(ex);
        inv = mat.matModInv(key);
    }

    /**
     * Encrypts a String using key
     * @param s message to encrypt
     * @return  encrypted message
     */
    public String encrypt(String s)
    {
        float[][][] string = mat.transformTextToMat(s);
        String enc = "";
        for (float[][] a : string)
        {
            float[][] part = mat.matrixMultiply(key, a);
            part = mat.modMat(part, 97);
            enc += mat.transformMatToText(part);
        }
        return enc;
    }

    /**
     * Decrypts a String using key
     * @param s message to decrypt
     * @return  decrypted message
     */
    public String decrypt(String s)
    {
        float[][][] string = mat.transformTextToMat(s);
        String dec = "";
        for (float[][] a : string)
        {
            float[][] part = mat.matrixMultiply(inv, a);
            part = mat.modMat(part, 97);
            dec += mat.transformMatToText(part);
        }
        return dec;
    }

    /**
     * Gets the key values from matrix of ClientKeyExchanges
     * @param ex    matrix of ClientKeyExchanges
     */
    private void getKey(ClientKeyExchange[][] ex)
    {
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                ClientKeyExchange e = ex[i][j];
                key[i][j] = e.getKey();
            }
        }
    }
}