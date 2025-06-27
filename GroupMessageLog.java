/*
 * Extends the message log 
 * for logging message in group
 */
public class GroupMessageLog extends MessageLog {
    /**
     * Set the path, when logging shows the group name as the log file name
     * @param path String
     */
    public GroupMessageLog(String path) {
        super();
        setLogPath(path);
    }
}