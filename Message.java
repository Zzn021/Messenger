import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {
    private String message;
    private String username;
    private LocalDateTime timestamp;


    /**
     * Message object constructor. Log the time the message object being created as the timestamp
     * @param message String 
     * @param username String
     */
    public Message(String message, String username) {
        this.message = message;
        this.username = username;
        this.timestamp = LocalDateTime.now();
    }


    /**
     * Get the message
     * @return String message
     */
    public String getMessage() {
        return message;
    }


    /**
     * Get the username
     * @return String username
     */
    public String getUsername() {
        return username;
    }


    /**
     * Get the timestamp as String
     * @return String timestamp
     */
    public String getTimestamp() {
        return timeToString();
    }


    /**
     * Convert the DateTimeFormatter to string
     * @return String timestamp
     */
    private String timeToString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
        return this.timestamp.format(formatter);
    }


    /**
     * Format print of the message
     * TIMESTAMP; USERNAME; MESSAGE
     */
    @Override
    public String toString() {
        return timeToString() + "; " + username + ";" + message;
    }
}
