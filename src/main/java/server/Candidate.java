package server;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * The Candidate Class represents an object containing the information of servers in a cluster,
 * and is in turn contained within the Ballot object to be passed amongst cluster members
 */
public class Candidate extends Object implements Serializable {
    private static final long serialVersionUID = 8850092555201204638L;
    String ip;
    Long pid;

    /**
     * Candidate constructor
     * @param ip String of IP of new Candidate
     * @param pid Long, PID of new Candidate
     * @throws RemoteException
     */
    public Candidate(String ip, Long pid) throws RemoteException {
        super();
        this.ip = ip;
        this.pid = pid;
    }

    /**
     * Get function for Candidate PID
     * @return Long, PID of Candidate
     */
    public Long getPID(){
        return this.pid;
    }

    /**
     * Get function for Candidate IP
     * @return String of Candidate IP
     */
    public String getIP(){
        return this.ip;
    }

    /**
     * ToString function for Candidate IP
     * @return String of Candidate IP
     */
    public String toString(){
        return this.ip;
    }
}
