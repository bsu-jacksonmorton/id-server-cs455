package server;

import java.io.Serializable;

/**
 * The DatabaseOperation represents a Serializable Object that the Coordinator can send to follower Servers for the
 * purposes of replicating operations on a Database, and was Extended into the various different kinds of Operations
 * Clients are capable of requesting
 */
public class DatabaseOperation implements Serializable {
    private Integer timestamp;

    /**
     * Constructor
     * @param timestamp Integer the current timestamp of the server completing the action
     */
    public DatabaseOperation(Integer timestamp){
        this.timestamp = timestamp;
    }

    /**
     * Method to obtain the timestamp packaged in the Object
     * @return Integer the timestamp in the Object
     */
    public Integer getTimestamp() {
        return timestamp;
    }
}
