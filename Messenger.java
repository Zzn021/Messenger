import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/*
 * helps to send and receive message between server and client
 * Encapsulate the dataInputStream and dataOutputStream and provide more features
 */
public class Messenger {
    private DataInputStream dataInputStream = null;
    private DataOutputStream dataOutputStream = null;


    /**
     * Messenger object constructor
     * Opens an input and an output stream
     * @param socket TCP socket
     */
    public Messenger(Socket socket) {
        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Read a message from input stream and cast to String
     * @return String
     * @throws IOException
     */
    public String readMessage() throws IOException {
        return (String) dataInputStream.readUTF();
    }


    /**
     * Send a message with given String msg and then wait for reply
     * @param msg String message
     * @return String 
     * @throws IOException
     */
    public String readMessage(String msg) throws IOException {
        sendMessage(msg);
        return readMessage();
    }


    /**
     * Test if there is any bytes to be read 
     * @return boolean
     * @throws IOException
     */
    public boolean isEmpty() throws IOException {
        if (dataInputStream.available() > 0) {
            return false;
        }
        return true;
    }


    /**
     * Send a Message 
     * @param message String message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        dataOutputStream.writeUTF(message);
        dataOutputStream.flush();
    }


    /**
     * Close the input and output stream
     */
    public void close() {
        try {
            dataOutputStream.close();
            dataInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Check if input and output stream are disconnected
     * @throws IOException
     */
    public void healthCheck() throws IOException {
        assert dataInputStream != null;
        assert dataOutputStream != null;
    }
}
