package server;

import java.io.Serializable;
import java.util.UUID;

/**
 * The DatabaseCreate Object represents a Serializable Object that the Coordinator can send to Followers for the
 * purposes of replicating a Create User operation requested by a Client
 */
public class DatabaseCreate extends DatabaseOperation implements Serializable {
    private String loginName;
    private String realName;
    private String password;

    private String ip;
    private UUID uuid;

    /**
     * Constructor
     * @param timestamp Integer Current timestamp of the server
     * @param loginName String Username requested by the Client
     * @param realName String real name given by the Client
     * @param password String password requested for the account by the Client
     * @param ip String IP of the server performing the Operation
     * @param uuid UUID of the account created by the Server
     */
    public DatabaseCreate(Integer timestamp, String loginName, String realName, String password, String ip, UUID uuid) {
        super(timestamp);
        this.loginName = loginName;
        this.realName = realName;
        this.password = password;
        this.ip = ip;
        this.uuid = uuid;
    }

    /**
     * Method to obtain the packaged username in the Object
     * @return String username contained in the Object
     */
    public String getLoginName() {
        return loginName;
    }

    /**
     * Method to obtain the packaged username in the Object
     * @return String realName contained in the Object
     */
    public String getRealName() {
        return realName;
    }

    /**
     * Method to obtain the packaged password in the Object
     * @return String password contained in the Object
     */
    public String getPassword() {
        return password;
    }

    /**
     * Method to obtain the IP of the server that handled the Create Request
     * @return String IP of the server that handled the Create Request
     */
    public String getIp() {
        return ip;
    }

    /**
     * Method to obtain the UUID packaged in the Object
     * @return UUID contained in the Object
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Method to obtain the User packaged in the Object
     * @return User contained in the Object
     */
    public User getUser(){
        return new User(this.loginName,this.realName,this.password,this.ip,this.uuid);
    }

    /**
     * Overloaded constructor, to be given a specific timestamp
     * @param timestamp Int representing requested timestamp
     */
    public DatabaseCreate(Integer timestamp){
        super(timestamp);
    }

}
