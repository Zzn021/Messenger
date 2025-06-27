import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
 * Helps to manage all the active users and how to contact them
 * Also log the user's activities in userlog.txt
 */
public class UserLog {
    private String logPath = "userlog.txt";
    private ArrayList<UserRecord> activeUser = new ArrayList<>();
    private Map<String, Messenger> userContacts = new HashMap<>();


    /**
     * Append a user to the active user list
     * and record the user's messenger.
     * @param record UserRecord
     * @param messenger Messenger
     */
    public void appendUser(UserRecord record, Messenger messenger) {
        this.activeUser.add(record);
        this.userContacts.put(record.getUsername(), messenger);
        log();
    }
    

    /**
     * Test if a user is active
     * @param username String
     * @return boolean
     */
    public boolean isActive(String username) {
        if (findUser(username) != null) return true;
        return false;    
    }


    /**
     * Get the user's messenger
     * @param username String 
     * @return Messenger
     */
    public Messenger getUserContact(String username) {
        return userContacts.get(username);
    }


    /**
     * Get all active users' UserRecord excludes the current user
     * @param currentUser String current user
     * @return ArrayList<UserRecord>
     */
    public ArrayList<UserRecord> getAllActiveUsers(String currentUser) {
        ArrayList<UserRecord> all = new ArrayList<>();
        for (int i = 0; i < activeUser.size(); i++) {
            if (!activeUser.get(i).getUsername().equals(currentUser)) {
                all.add(activeUser.get(i));
            }
        }
        return all;
    }


    /**
     * Get the target user's UDP port
     * @param username String targer user
     * @return Integer UDP port number
     */
    public Integer getUserUDP(String username) {
        UserRecord r = findUser(username);
        if (r != null) {
            return r.getUDPport();
        }
        return null;
    }

    
    /**
     * Logout a user delete the record in userlog.txt
     * remove from active user list
     * @param username String
     */
    public void logoutUser(String username) {
        UserRecord u = findUser(username);
        if (u != null) {
            activeUser.remove(u);
            log();
        }
    }


    /**
     * Finds a user in active user list
     * @param username String user name
     * @return UserRecord (On fail null)
     */
    private UserRecord findUser(String username) {
        for (int i = 0 ; i < activeUser.size(); i++) {
            if (activeUser.get(i).getUsername().equals(username)) {
                return activeUser.get(i);
            }
        }
        return null;    // on fail
    }


    /**
     * Log the user activity (login / logout) in userlog.txt
     */
    private void log() {
        // Write to userlog.txt
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logPath))) {
            for (int i = 0; i < activeUser.size(); i++) {
                UserRecord user = activeUser.get(i);
                String entry = String.format("%d; %s", i + 1, user.toString());
                writer.write(entry);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
