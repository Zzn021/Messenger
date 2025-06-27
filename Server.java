import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.io.*;

/*
 * The Server
 */
public class Server {
    private static final int BLOCK_DURATION = 10;
    private static Integer serverPort;
    private static Integer MAX_ATTEMPT;
    private static ServerSocket serverSocket;
    private static GroupLog groupLog = new GroupLog();                  // Manage groups
    private static MessageLog messageLog = new MessageLog();            // Manage messages
    private static Map<String, Long> blockedUser = new HashMap<>();     // Manage blocked users
    private static UserLog userLog = new UserLog();                     // Manage users


    /**
     * Add the user address to blocked user dictionary
     * The duration is 10 seconds
     * @param address String user address
     */
    private static void blockUser(String address) {
        blockedUser.put(address, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(BLOCK_DURATION));
    }


    /**
     * Test if a user is currently blocked
     * @param address String user address
     * @return boolean
     */
    private static boolean isBlocked(String address) {
        Long unblockTime = blockedUser.get(address);
        return unblockTime != null && System.currentTimeMillis() < unblockTime;
    }


    /**
     * Log the user's action in server terminal 
     * @param user String username
     * @param cmd Command the user's action
     */
    private static void issueMsg(String user, Command cmd) {
        System.out.println(user + " issued " + cmd.getAction() + " command");
    }


    /**
     * Broadcast the given message in server terminal
     * @param msg String message
     */
    private static void broadcast(String msg) {
        System.out.println(msg);
    }


    /**
     * Log the server's return message in terminal
     * @param message String return message
     */
    private static void returnMsg(String message) {
        System.out.println("Return message:\n" + message);
    }


    /*
     * Multi-Threaded for multiple client
     */
    private static class ClientThread extends Thread {
        private final Socket clientSocket;
        private boolean clientAlive = false;
        private String clientName;
        private Messenger messenger = null;


        /**
         * ClientThread Constructor
         * @param clientSocket Socket client socket
         */
        ClientThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            this.messenger = new Messenger(clientSocket);;
        }


        /**
         * Start the client thread
         */
        @Override
        public void run() {
            super.run();
            int clientPort = clientSocket.getPort();
            String clientAddress = clientSocket.getInetAddress().getHostAddress();
            String clientID = "("+ clientAddress + ", " + clientPort + ")";

            broadcast("===== New connection created for user - " + clientID);
            try {
                // Login
                int failAttempts = 0;
                Authenticator authenticator = new Authenticator();
                while (!clientAlive) {
                    if (!isBlocked(clientID)) {
                        String userName = messenger.readMessage("Please enter username:");
                        String password = messenger.readMessage("Please enter password:");
    
                        if (authenticator.login(userName, password)) {
                            broadcast("user verified:" + clientID);
                            // Tell the client to send the UDP port number
                            Integer UDPport = Integer.parseInt(messenger.readMessage("LOGIN_SUCCEEDED"));
                            // Log the user
                            userLog.appendUser(new UserRecord(userName, clientID, UDPport), messenger);
                            messenger.sendMessage("Welcome " + userName);
                            clientName = userName;
                            clientAlive = true;
                        } else {
                            failAttempts++;
                            messenger.sendMessage("Wrong username or password");
                            if (failAttempts == MAX_ATTEMPT) {
                                messenger.sendMessage("max attempts reached please wait for 10 seconds");
                                blockUser(clientID);                   
                                failAttempts = 0;
                            }
                        }
                    } else {
                        // Block the user for 10 seconds.
                        if (!messenger.isEmpty()) {
                            messenger.readMessage();
                            messenger.sendMessage("Please try again later.");
                        }
                    }
                }

                // Logged in
                while (clientAlive) {
                    messenger.healthCheck();
                    String menu = "/msgto /activeuser /creategroup /joingroup /groupmsg /logout /p2pvideo\nPlease enter your command:";
                    Command command = new Command(messenger.readMessage(menu));
                    switch (command.getAction()) {
                        case "/msgto":                            
                            msgto(command);
                            break;
                        case "/activeuser":
                            activeuser(command);
                            break;
                        case "/creategroup":
                            createGroup(command);
                            break;
                        case "/joingroup":
                            joinGroup(command);
                            break;
                        case "/groupmsg":
                            groupMsg(command);
                            break;
                        case "/logout":
                            logout(command);
                            break;
                        case "/p2pvideo":
                            break;
                        default:
                            wrongCommand();
                            break;
                    }    
                }
            } catch (EOFException e) {
                broadcast("===== the user disconnected, user - " + clientID);
                clientAlive = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        
        /**
         * User input invalid command send response message to the user
         * @throws IOException
         */
        private void wrongCommand() throws IOException {
            messenger.sendMessage("Error, Invalid command!");
        }


        /**
         * User input invalid command send customized response message to the user 
         * Also log the message as return message in server terminal
         * @param msg String msg
         * @throws IOException
         */
        private void wrongCommand(String msg) throws IOException {
            returnMsg(msg);
            messenger.sendMessage(msg);
        }


        /**
         * Logout the user. 
         * Requirements: No Argument accepted.
         * @param cmd Command the user command
         * @throws IOException
         * @throws EOFException
         */
        private void logout(Command cmd) throws IOException, EOFException {
            issueMsg(clientName, cmd);
            // Error checking
            if (cmd.getArgSize() != 0) {
                wrongCommand("Error:/logout: Too many arguments");
                return;
            }
            userLog.logoutUser(clientName);
            messenger.sendMessage("Bye, " + clientName + "!");
            messenger.sendMessage("LOGOUT");    // Tell the client to close connect
            broadcast(clientName + " logout");
            throw new EOFException();   
        }


        /**
         * Send message in a group. 
         * Creats new Message and log in group log file
         * Requirements:
         *  1. Arguments >= 2
         *  2. Group exists
         *  3. Client is a member of the group
         * @param cmd Command user command (including the message)
         * @throws IOException
         */
        private void groupMsg(Command cmd)throws IOException {
            issueMsg(clientName, cmd);
            // Check argument number
            if (cmd.getArgSize() < 2) {
                wrongCommand("Error:/groupmsg: Check arguments");
                return;
            }

            String groupname = cmd.getArg(0);
            String message = "";
            for (int i = 1; i < cmd.getArgSize(); i++) {
                message += " " +  cmd.getArg(i);
            }

            if (!groupLog.isCreated(groupname)) {
                wrongCommand("The group chat " + groupname + " does not exist.");
                return;
            } 
            Group group = groupLog.getGroup(groupname);
            if (!group.isMember(clientName)) {
                wrongCommand("You are not in this group chat: " + groupname);
                return;
            }
            Message msg = new Message(message, clientName);
            group.sendMessage(msg, userLog);    // Will Log in group log file
            messenger.sendMessage("message sent at " + msg.getTimestamp() + ".");
            // broadcast
            broadcast(clientName + " message to " + groupname + ":" + message + ", at " + msg.getTimestamp());
        }


        /**
         * The client join a group
         * Requirements:
         *  1. Arguments = 1
         *  2. Group exists
         * @param cmd Command user command
         * @throws IOException
         */
        private void joinGroup(Command cmd) throws IOException {
            issueMsg(clientName, cmd);
            // Check arguments
            if (cmd.getArgSize() != 1) {
                wrongCommand("Error:/joingroup: Not enough arguments");
                return;
            }

            String groupname = cmd.getArg(0);
            if (!groupLog.isCreated(groupname)) {
                wrongCommand("Error:/joingroup: Group doesn't exists");
                return;
            }                            
            // Join the group
            Group target = groupLog.getGroup(groupname);
            target.join(clientName);
            messenger.sendMessage( "Join group chat: " + groupname + " successfully\n");
            returnMsg("Join group chat: " + groupname + "successfully, users: " + target.toString() + "\n");
        }


        /**
         * The client create a group with other active users
         * Creats new Group object and register in GroupLog
         * Requirements:
         *  1. Arguments >= 2
         *  2. Group name matches the format
         *  3. Group name is not taken
         *  4. all members are active
         * @param cmd Command 
         * @throws IOException
         */
        private void createGroup(Command cmd) throws IOException {
            issueMsg(clientName, cmd);
            // Not enough arguments
            if (cmd.getArgSize() < 2) {
                wrongCommand("Error:/creategroup: Not enough arguments");
                return;
            }

            String regex = "^[a-zA-Z0-9]+$";
            String groupName = cmd.getArg(0);
            // Invalid group name format
            if (!groupName.matches(regex)) {
                wrongCommand("Error:/creategroup: Invalid group name");
                return;
            // Group Exists
            } else if (groupLog.isCreated(groupName)) {
                wrongCommand("Error:/creategroup: Failed to create the group chat " + groupName + " groupname exists");
                return;
            }

            Group newGroup = new Group(cmd, clientName);
            // Group member not active
            if (!groupLog.registerGroup(newGroup, userLog)) {
                wrongCommand("Error:/creategroup: one of groupmember not active or invalid");
                return;
            }
            // On success
            String response = "Group chat room created, name " + groupName + " users: " + newGroup.toString();
            messenger.sendMessage(response);
            returnMsg(response);
        }


        /**
         * Get all the active users currently
         * Requirements:
         *  1. No Arguments required
         * If no other user, send "no other active user"
         * @param cmd Command
         * @throws IOException
         */
        private void activeuser(Command cmd) throws IOException {
            issueMsg(clientName, cmd);
            // Check argument number
            if (cmd.getArgSize() != 0) {
                wrongCommand("Error:/activeuser: Too many arguments");
                return;
            }
            String report; 
            ArrayList<UserRecord> allActiveUser = userLog.getAllActiveUsers(clientName);
            if (allActiveUser.size() == 0) {
                report = "no other active user";
            } else {
                report = allActiveUser.stream().map(UserRecord::toString).collect(Collectors.joining("\n"));
            }
            returnMsg(report);
            // send the report
            messenger.sendMessage(report);
        }


        /**
         * Private message to another active user
         * Create new Message object and log in messagelog.txt
         * Requirements:
         *  1. Arguments >= 2
         *  2. Cannot msgto self
         *  3. The receiver has to be active
         * @param cmd Command
         * @throws IOException
         */
        private void msgto(Command cmd) throws IOException {
            issueMsg(clientName, cmd);
            // Check argument number
            if (cmd.getArgSize() < 2) {
                wrongCommand("Error:/msgto: check arugments");
                return;
            }
            // The receiver name:
            String receiver = cmd.getArg(0);
            // Create the message from arguments:
            String message = "";
            for (int i = 1; i < cmd.getArgSize(); i++) {
                message += " " +  cmd.getArg(i);
            }

            // Error check: receiver is not self, receiver is active
            if (receiver.equals(clientName)) {
                wrongCommand("Error:/msgto: Can't /msgto yourself");
                return;
            } else if (!userLog.isActive(receiver)) {
                wrongCommand("Error:/msgto: " + receiver + " is not active");
                return;
            }
            Message msg = new Message(message, receiver);
            messageLog.logMessage(msg);     // Log message in message.txt
            Messenger userContact = userLog.getUserContact(receiver);
            // Send message to target
            userContact.sendMessage(msg.getTimestamp() + ", " + clientName + ":" + msg.getMessage());
            // Notify the sender
            messenger.sendMessage("message sent at " + msg.getTimestamp() + ".");
            // broadcast
            broadcast(clientName + " message to " + receiver + ":" + msg.getMessage() + ", at " + msg.getTimestamp());
        }
    }
    
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            broadcast("===== Error usage: java Server SERVER_PORT number_of_consecutive_failed_attempts =====");
            return;
        }

        serverPort = Integer.parseInt(args[0]);
        serverSocket = new ServerSocket(serverPort);
        try {
            if (Integer.parseInt(args[1]) < 6 &&  Integer.parseInt(args[1]) > 0) {
                MAX_ATTEMPT = Integer.parseInt(args[1]);
            } else {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            broadcast("Invalid number of allowed failed consecutive attempt:" + args[1]);
            return;
        }
        
        broadcast("===== Server is running =====");
        broadcast("===== Waiting for connection request from clients...=====");

        while (true) {
            // when new connection request reaches the server, then server socket establishes connection
            Socket clientSocket = serverSocket.accept();
            ClientThread clientThread = new ClientThread(clientSocket);
            clientThread.start();
        }
    }
}