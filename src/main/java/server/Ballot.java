package server;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The Ballot class represents a Serializable object that servers that
 * are part of a cluster can pass to each other to facilitate an election
 */
public class Ballot implements Serializable {
    private static final long serialVersionUID = 8850092555201204638L;
    String authorIP;
    Long authorPID;
    ArrayList<Candidate> candidates;

    /**
     * Ballot Constructor
     * @param authorIP The IP of the sending server
     * @param authorPID The PID of the sending author
     * @throws RemoteException
     */
    public Ballot(String authorIP, Long authorPID) throws RemoteException {
        super();
        this.authorIP = authorIP;
        this.authorPID = authorPID;
        this.candidates = new ArrayList<>();
        candidates.add(new Candidate(this.authorIP, this.authorPID));
    }

    /**
     * Sender IP get() method
     * @return IP of sender
     */
    public String getAuthorIP() {
        return authorIP;
    }

    /**
     * Appends a new server to the ballot to be weighed in the election, to find the highest PID among them
     * @param candidate New server added
     */
    public void addToCandidates(Candidate candidate){
        candidates.add(candidate);
    }
    public String electCoordinator(){
       Candidate coordinator = candidates.remove(0);
        for(Candidate candidate : candidates){
            if(coordinator.getPID() < candidate.getPID()){
                coordinator = candidate;
            }
        }
        return coordinator.getIP();
    }

    /**
     * Method to list all Candidate servers on a Ballot
     * @return A String representing all Candidates
     */
    public String toString(){
        return candidates.toString();
    }

}
