package client;

/**
 * Opens connection with a chat server that is already open and creates a UI for user to interact with.
 *
 * @author Caroline Zeng
 * @version 1.0.0
 */
public class ClientRunner
{
    /**
     * Sets up connection and UI for user chat
     * @param args
     */
    public static void main(String[] args)
    {
        Client client      = new Client();
        ClientUI w         = new ClientUI(client);
        client.messageArea = w.messageArea;
        client.textField   = w.textField;
        client.start();
    }
}
