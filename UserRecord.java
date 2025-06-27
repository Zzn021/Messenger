import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 * Helps to store the user's basic information
 */
public class UserRecord {
    private LocalDateTime timestamp;    // Time the userrecord created
    private String username;
    private String address;
    private Integer UDPport;


    /**
     * UserRecord object constructor
     * @param username String 
     * @param address String 
     * @param UDPport Integer
     */
    public UserRecord(String username, String address, Integer UDPport) {
        this.timestamp = LocalDateTime.now();
        this.username = username;
        this.address = address;
        this.UDPport = UDPport;
    }


    /**
     * Get the timestamp cast to String 
     * @return String
     */
    public String getTimestamp() {
        return timeToString();
    }


    /**
     * Get username
     * @return String
     */
    public String getUsername() {
        return username;
    }


    /**
     * Get user address
     * @return String
     */
    public String getAddress() {
        return address;
    }


    /**
     * Get user UDP port number 
     * @return Integer
     */
    public Integer getUDPport() {
        return UDPport;
    }


    /**
     * Cast timestamp to string in dd MMM yyyy HH:mm:ss format
     * @return String
     */
    private String timeToString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
        return this.timestamp.format(formatter);
    }
    

    /** 
     * Print the record in this format:
     * 1; 01 Jun 2022 21:30:04; Yoda; 129.64.1.11; 6666
     */
    @Override
    public String toString() {
        return timeToString() + "; " + username + "; " +  address + "; " + UDPport;
    }
}