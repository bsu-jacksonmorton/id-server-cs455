package server;

import java.util.UUID;

/**
 * The DatabaseDelete Object represents a Serializable Object the Coordinator can send to other members of the
 * cluster for the purposes of replication
 */
public class DatabaseDelete extends DatabaseOperation{
    private String loginName;
    private String password;

    /**
     * Method to obtain the packaged username packaged in the Object
     * @return String username contained in the Object
     */
    public String getLoginName() {
        return loginName;
    }

    /**
     * Method to obtain the packaged password packaged in the Object
     * @return String password contained in the Object
     */
    public String getPassword() {
        return password;
    }

    /**
     * Constructor
     * @param timestamp Integer timestamp of the Delete Operation
     * @param loginName String username to be deleted from the Database
     * @param password String password of the account to be deleted from the Database
     */
    public DatabaseDelete(Integer timestamp, String loginName, String password) {
        super(timestamp);
        this.loginName = loginName;
        this.password = password;

    }


}
