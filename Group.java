import java.io.IOException;
import java.util.ArrayList;

/*
 * Group object used in the application
 * Manages the groupmembers and groupmessage log
 */
public class Group {
    private String groupName;
    private ArrayList<String> groupmembers = new ArrayList<>();
    private GroupMessageLog gml = new GroupMessageLog(getGroupName() + "_messageLog.txt");

    /**
     * Group object constructor
     * @param command Command the user's input
     * @param creator String the group creator
     */
    public Group(Command command, String creator) {
        this.groupName = command.getArg(0);
        gml = new GroupMessageLog(groupName + "_messageLog.txt");
        // Creator is the first member
        getGroupmembers().add(creator); 
        for (int i = 1; i < command.getArgSize(); i++) {
            getGroupmembers().add(command.getArg(i));
        }
    }


    /**
     * Return the group name
     * @return String groupName
     */
    public String getGroupName() {
        return groupName;
    }


    /**
     * Return the group creator
     * @return String creator
     */
    public String getCreator() {
        // Creator is the first member in list.
        return groupmembers.get(0);
    }


    /**
     * Return all the members
     * @return ArrayList<String> groupmembers
     */
    public ArrayList<String> getGroupmembers() {
        return groupmembers;
    }


    /**
     * Let the user join this group
     * @param username String the user about to join
     */
    public void join(String username) {
        if (!isMember(username)) {
            getGroupmembers().add(username);
        }
    }

    /**
     * Test if the given user is a member of this group
     * @param name String user
     * @return boolean
     */
    public boolean isMember(String name) {
        if (getGroupmembers().contains(name)) {
            return true;
        }
        return false;
    }

    
    /**
     * Send a message to every groupmember excludes the sender
     * @param msg Message the message
     * @param log UserLog userlog to get the user contacts
     * @throws IOException
     */
    public void sendMessage(Message msg, UserLog log) throws IOException {
        gml.logMessage(msg);    // Log the message in groupname_message.txt
        // For each member send the message
        for (String member : getGroupmembers()) {
            if (!member.equals(msg.getUsername())) {
                Messenger contact = log.getUserContact(member);
                contact.sendMessage(msg.getTimestamp() + ", " + getGroupName() + ", " + msg.getUsername() + ":" + msg.getMessage());
            }
        }
    }


    /**
     * When turn to string out put all the users.
     */
    @Override
    public String toString() {
        return String.join(", ", getGroupmembers());
    }
}
