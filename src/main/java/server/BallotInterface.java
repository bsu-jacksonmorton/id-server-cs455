package server;

import java.rmi.RemoteException;

/**
 * Interface method for Ballot object
 */
public interface BallotInterface extends java.rmi.Remote{
    public String electCoordinator() throws RemoteException;
    public String getAuthorIP() throws RemoteException;
    public void addToCandidates(Candidate candidate) throws RemoteException;

    }
