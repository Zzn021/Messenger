import java.net.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

/*
 * The Client
 */
public class Client {
    private static String serverHost;
    private static Integer serverPort;
    private static Integer clientUDPport;
    private static ArrayList<String> pastMsgs = new ArrayList<>();
    private static String clientName;
    private static final Integer SIZE = 1024; // packet size


    /**
     * Get the past messages received from Server (grows downwards)
     * @param i Integer an index relative to the last message
     * @return String message
     */
    public static String getLastMsg(Integer i) {
        if (pastMsgs.size() != 0 ) {
            return pastMsgs.get(pastMsgs.size() -1 - i);
        }
        return null;
    }


    /**
     * Append to the pastMsgs array
     * @param lastMsg String
     */
    public static void addLastMsg(String lastMsg) {
        Client.pastMsgs.add(lastMsg);
    }


    /**
     * Get the client name
     * @return String client name
     */
    public static String getClientName() {
        return clientName;
    }


    /**
     * Set the client name
     * @param name String
     */
    public static void setClientName(String name) {
        Client.clientName = name;
    }


    /**
     * Get the UDP port number and address of the target user. 
     * By using the past messages (Assuming the Client use /activeuser before /p2pvideo)
     * @param name String target user name
     * @return Audience contains the port and address
     */
    private static Audience getUDP(String name) {
        String[] contacts = getLastMsg(1).split("\n");
        for (String contact : contacts) {
            String[] field = contact.split("; ");
            // Name should be in the second column
            if (field[1].equals(name)) {
                // Get port
                Integer port = Integer.parseInt(field[3]);
                // Get address
                String address;
                Pattern pattern = Pattern.compile("\\(([^,]+),\\s*\\d+\\)");
                Matcher matcher = pattern.matcher(field[2]);
                if (matcher.find()) {
                    address = matcher.group(1);
                    System.out.println(address);
                    Audience aud = null; 
                    try {
                        aud = new Audience(port, address);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return aud;
                }
            }
        }
        return null;    // On fail
    }


    /**
     * Send the file in packets. The header tells the filename and sender
     * The last packet indicating end of transfer
     * @param filePath String filepath
     * @param socket DatagramSocket UDP socket
     * @param audience Audience target user's updport and address
     * @throws IOException
     */
    private static void sendFile(String filePath, DatagramSocket socket, Audience audience) throws IOException {
        File file = new File(filePath);
        FileInputStream fileInputStream = new FileInputStream(file);
    
        byte[] buffer = new byte[SIZE];
        int bytesRead;
    
        // Include metadata in the first packet as a header
        String metadata = "USERNAME=" + getClientName() + "&FILENAME=" + file.getName();
        InetAddress address = audience.getAddress();
        Integer port = audience.getUdpPort();
        
        byte[] header = metadata.getBytes();
        DatagramPacket headerPacket = new DatagramPacket(header, header.length, address, port);
        socket.send(headerPacket);
    
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            DatagramPacket packet = new DatagramPacket(buffer, bytesRead, address, port);
            socket.send(packet);
            // Clear the buffer for the next chunk
            buffer = new byte[SIZE];
        }
    
        // Signal the end of the file by sending an empty packet
        DatagramPacket endPacket = new DatagramPacket(new byte[0], 0, address, port);
        socket.send(endPacket);
    
        // Close resources
        fileInputStream.close();
        System.out.println(file.getName() + "  has been uploaded");
    }


    /**
     * Receive file from UDP port.
     * @param socket DatagramSocket
     * @throws IOException
     */
    private static void receiveFile(DatagramSocket socket) throws IOException {
        byte[] buffer = new byte[SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
    
        // Receive the header to get sender information
        socket.receive(packet);
        String header = new String(packet.getData(), 0, packet.getLength());

        // Extract username and filename from the header
        String[] headerParts = header.split("&");
        String username = headerParts[0].split("=")[1];
        String filename = headerParts[1].split("=")[1];

        try (FileOutputStream fileOutputStream = new FileOutputStream(filename)) {
            while (true) {
                socket.receive(packet);
    
                // Check for the end of the file (empty packet)
                if (packet.getLength() == 0) {
                    break;
                }
    
                // Write the received chunk to the file
                fileOutputStream.write(packet.getData(), 0, packet.getLength());
            }
            fileOutputStream.close();

            System.out.println("Received file '" + filename + "' from: " + username);
            String menu = "/msgto /activeuser /creategroup /joingroup /groupmsg /logout /p2pvideo\nPlease enter your command:";
            System.out.println(menu);
        }
    }


    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("===== Error usage: java TCPClient SERVER_IP SERVER_PORT =====");
            return;
        }

        serverHost = args[0];
        serverPort = Integer.parseInt(args[1]);
        clientUDPport = Integer.parseInt(args[2]);

        // define socket for client
        Socket clientSocket = new Socket(serverHost, serverPort);   // TCP
        DatagramSocket udpSocket = new DatagramSocket(clientUDPport);   // UDP
        Messenger messenger = new Messenger(clientSocket);
        
        // TCP server Listener Thread
        Thread receiveThread = new Thread(() -> {
            try {
                while (true) {
                    String msg = messenger.readMessage();
                    if (msg.split(" ")[0].equals("Welcome")) {
                        setClientName(msg.split(" ")[1]);
                    }
                    switch (msg) {
                        case "LOGIN_SUCCEEDED":
                            // Tell the server the client's upd port number
                            messenger.sendMessage(clientUDPport.toString());
                            break;
                        case "LOGOUT":
                            // Close the client application
                            System.out.println("Connection closed");
                            clientSocket.close();
                            messenger.close();
                            System.exit(0);
                            break;
                        default:
                            System.out.println(msg);
                            break;
                    }
                    addLastMsg(msg);    // appende past message
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        receiveThread.start();

        // UDP server Listener Thread
        Thread udpReceiveThread = new Thread(() -> {
            try{
                while (true) {
                    receiveFile(udpSocket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        udpReceiveThread.start();

        // define a BufferedReader to get input from command line i.e., standard input from keyboard
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {                    
                // read input from command line
                String message = reader.readLine();
                Command cmd = new Command(message);
                if (cmd.getAction().equals("/p2pvideo")) {
                    if (cmd.getArgSize() != 2) {
                        System.out.println("Error:/p2pvideo: check arguments");
                    } else {
                        Audience audience = getUDP(cmd.getArg(0));
                        String path = cmd.getArg(1);
                        if (audience == null) {
                            System.out.println(cmd.getArg(0) + " is not active");
                        } else {
                            sendFile(path, udpSocket, audience);
                        }
                    }
                }
                // write message into dataOutputStream and send/flush to the server
                messenger.sendMessage(cmd.toString());
            } catch (EOFException e) {
                System.out.println("Connection closed");
                clientSocket.close();
                messenger.close();
                break;
            } 
        }
    }
}