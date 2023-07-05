package server;

import java.util.UUID;

/**
 * The DatabaseUpdate Object represents a Serializable Object the coordinator can send to follower servers in the
 * cluster for the purposes of replicating Modify operations on the Database
 */
public class DatabaseUpdate extends DatabaseOperation{
    private String loginName;
    private String updatedLoginName;
    private String password;

    /**
     * Method to obtain the username to be changed on the Database
     * @return String the username to be changed
     */
    public String getLoginName() {
        return loginName;
    }

    /**
     * Method to obtain the username to change the old username to
     * @return String the username to be changed to
     */
    public String getUpdatedLoginName() {
        return updatedLoginName;
    }

    /**
     * Method to obtain the password of the account to be modified
     * @return String password of the account to be modified
     */
    public String getPassword() {
        return password;
    }

    /**
     * Construdctor
     * @param timestamp Integer the current timestamp at time of operation
     * @param loginName String the username to be changed
     * @param updatedLoginName String the username to be changed to
     * @param password String the password of the account being modified
     */
    public DatabaseUpdate(Integer timestamp, String loginName, String updatedLoginName, String password) {
        super(timestamp);
        this.loginName = loginName;
        this.updatedLoginName = updatedLoginName;
        this.password = password;
    }


}
