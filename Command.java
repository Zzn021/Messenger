import java.util.ArrayList;

/*
 * Help to process the user's input.
 */
public class Command {
    private String action;
    private ArrayList<String> arguments = new ArrayList<>();
    private String fullMessage;


    /**
     * Command object constructor
     * @param cmd String the user's input
     */
    public Command(String cmd) {
        String[] args = cmd.split(" ");
        this.action = args[0];
        this.fullMessage = cmd;
        // Arguments start from the second item in args
        for (int i = 1; i < args.length; i++) {
            arguments.add(args[i]);
        }   
    }


    /**
     * Returns the user action (e.g.: /logout, /msgto, /creategroup ... etc)
     * @return String action
     */
    public String getAction() {
        return action;
    }


    /**
     * Get the arguments after the action.
     * The action itself is not counted as arguments.
     * @param i Integer the index of the targeting argument
     * @return String argument
     */
    public String getArg(Integer i) {
        return arguments.get(i);
    }


    /**
     * Get the number of arguments in total
     * @return Integer number of arguments
     */
    public Integer getArgSize() {
        return arguments.size();
    }


    /*
    * Convert a message to String.
    */
    @Override
    public String toString() {
        return fullMessage;
    }
}
