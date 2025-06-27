import java.net.InetAddress;
import java.net.UnknownHostException;

/*
 * encapsulate the udpport and address
 */
public class Audience {
    private Integer udpPort;
    private InetAddress address;

    
    /**
     * Constructor
     * Creates an Audience object
     * @param port Integer udp port number
     * @param addr String the ip address in String
     * @throws UnknownHostException
     */
    public Audience(Integer port, String addr) throws UnknownHostException{
        this.udpPort = port;
        this.address = InetAddress.getByName(addr);
    }


    /**
     * Return UDP port
     * @return Integer the UDP port
     */
    public Integer getUdpPort() {
        return udpPort;
    }


    /**
     * Reutrn the InetAddress
     * @return InetAddress the ipaddress
     */
    public InetAddress getAddress() {
        return address;
    }
}
