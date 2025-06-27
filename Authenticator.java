import java.io.BufferedReader;
import java.io.FileReader;

/*
 *  Helps to Authenticate the user.
 */
public class Authenticator {
    private static final String data = "credentials.txt";

    
    /**
     * Try to verify the user in credentials.txt
     * @param userName String the username to be verified
     * @param userPassword String the password to be verified
     * @return boolean
     */
    public boolean login(String userName, String userPassword) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(data));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\s+");
                String name = parts[0];
                String password = parts[1];
                if (userName.equals(name) && userPassword.equals(password)) {
                    br.close();
                    return true;
                }
            }
            br.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }
}