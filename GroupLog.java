import java.util.HashMap;
import java.util.Map;

/*
 * Manage all the existing groups
 */
public class GroupLog {
    private Map<String, Group> groups = new HashMap<>(); // Store all the created groups

    
    /**
     * Register a group, if matchs the requirment, then add to the created groups
     * The requirements:
     *  1.Creator is active
     *  2.All member are active
     * @param g Group group to be add
     * @param log UserLog helps to check if user is active
     * @return boolean
     */
    public boolean registerGroup(Group g, UserLog log) {
        if (!checkGroup(g, log)) {
            return false;
        }
        groups.put(g.getGroupName(), g);
        return true;
    }


    /**
     * Check if the group matches the requirement mentioned in registerGroup() (The above function)
     * @param g Group 
     * @param log UserLog 
     * @return boolean
     */
    private boolean checkGroup(Group g, UserLog log) {
        if (log.isActive(g.getCreator())) {
            for (String groupmember : g.getGroupmembers()) {
                if (! log.isActive(groupmember)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }


    /**
     * Test if a groupname is already being used
     * @param groupname String 
     * @return boolean
     */
    public boolean isCreated(String groupname) {
        if (groups.containsKey(groupname)) {
            return true;
        }
        return false;
    }


    /**
     * Get a Group object with groupname as key
     * @param groupname String the groupname
     * @return Group the group object (On Fail return null)
     */
    public Group getGroup(String groupname) {
        if (groups.containsKey(groupname)) {
            return groups.get(groupname);
        }    
        return null;
    }
}
