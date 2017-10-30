package client;

/**
 * Runs the Client
 *
 * @author Caroline Zeng
 * @version 1.0.0
 */
public class ClientRunner
{
    public static void main(String[] args)
    {
        Client client      = new Client();
        ClientUI w         = new ClientUI(client);
        client.messageArea = w.messageArea;
        client.textField   = w.textField;
        client.start();
    }
}
