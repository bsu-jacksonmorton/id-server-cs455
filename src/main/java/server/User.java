package server;

import java.util.Date;
import java.util.UUID;

/**
 * The User class is used to store User information for the IdServer database.
 *  * @author JMORTON
 *  * @author JCROWELL
 *  * @version 1.0 CS455 Spring 2022
 */
public class User implements java.io.Serializable {
    private String loginName;
    private String realName;
    private String passHash;
    private String IP;
    private UUID uuid;
    private Date lastModified;

    /**
     * Constructor
     * @param loginName
     * @param realName
     * @param passHash
     * @param IP
     * @param uuid
     */
    public User(String loginName, String realName, String passHash, String IP, UUID uuid){
        this.IP = IP;
        this.loginName = loginName;
        this.realName = realName;
        this.uuid = uuid;
        this.passHash = passHash;
        this.lastModified = new Date();
    }

    /**
     * Returns this user's ip address.
     * @return String ip
     */
    public String getIPAddress() {return IP;}

    /**
     * Returns this user's login name.
     * @return String login
     */
    public String getLoginName() {return loginName;}

    /**
     * Sets this user's login name.
     * @param updatedName
     */
    public void setLoginName(String updatedName){this.loginName = updatedName; this.lastModified = new Date();}

    /**
     * Sets this users UUID
     * @param uuid
     */
    public void setUUID(UUID uuid){this.uuid = uuid; this.lastModified = new Date();}

    /**
     * Returns this user's UUID
     * @return UUID uuid
     */
    public UUID getUUID() {return uuid;}

    /**
     * Returns the password hash for this user.
     * @return String hash
     */
    public String passTheHash(){return passHash;}

    @Override
    public String toString(){
        String retVal =   "\n | Login: " + loginName +"\n";
        retVal += " | Name: " + realName +"\n";
        retVal += " | IP Address: " + IP + "\n";
        retVal += " | UUID: " + uuid + "\n";
        retVal += " | Last Modified: " + lastModified + "\n";

        return retVal;
    }
}