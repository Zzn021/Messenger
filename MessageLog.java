import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/*
 * Helps to log the message sent in server in file 
 */
public class MessageLog {
    private String logPath = "messagelog.txt";
    private ArrayList<Message> msgList = new ArrayList<>();


    /**
     * Get the log file path
     * @return String
     */
    public String getLogPath() {
        return logPath;
    }


    /**
     * Set the log file's path
     * @param logPath
     */
    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }


    /**
     * Log ta message to the logfile
     * The sequence is the index in the msgList
     * @param msg Message
     */
    public void logMessage(Message msg) {
        msgList.add(msg);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logPath, true))) {
            String entry = String.format("%d; %s", msgList.size(), msg.toString());
            writer.write(entry);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
