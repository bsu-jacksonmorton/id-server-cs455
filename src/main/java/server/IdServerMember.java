package server;

import shared.Id;

import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Interface for the purposes of outlining the RMI methods available for IdServerMember Object
 */
public interface IdServerMember extends java.rmi.Remote {
    void coordinator(String authorIp, String coordinatorIpAddress) throws java.rmi.RemoteException;
    void setCoordinatorIP(String coordinatorIP) throws java.rmi.RemoteException;
    String heartbeat() throws java.rmi.RemoteException;
    Integer getLamportTimestamp() throws java.rmi.RemoteException;
    void replicateWrite(DatabaseOperation operation) throws java.rmi.RemoteException;
    HashMap<Integer, DatabaseOperation> getCoordinatorLedger() throws java.rmi.RemoteException;
    Database getDB() throws java.rmi.RemoteException;
    void receiveBallot(Ballot ballot) throws RemoteException;


}
