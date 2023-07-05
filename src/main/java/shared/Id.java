package shared;
import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

/**
 * Baseline interface enumerating the functions available to both Servers and Clients for RMI
 */
public interface Id extends Remote{
    Integer getLamportTimestamp() throws RemoteException;
    String getCoordinatorIP() throws RemoteException;
    /**
     * Returns a String containing all of the current login-names stored in the server database.
     * @return String loginNames
     * @throws RemoteException
     */
    String getAllLoginNames() throws RemoteException;
    /**
     * Returns a String containing all of the current UUIDs stored in the server database.
     * @return String uuids
     * @throws RemoteException
     */
    String getAllUUIDs() throws RemoteException;
    /**
     * Returns a String containing all of the current User objects stored in the server database.
     * @return String users
     * @throws RemoteException
     */
    String getAllUsers() throws RemoteException;
    /**
     * Creates a new User object using the passed parameter and adds that User object to the server database if the given loginName is avaiable,
     * and returns a String containing a helpful message indicating whether the operation was successful or not.
     * @param loginName Login-name of the User.
     * @param realName Legal-name of the User.
     * @param password Password hash digest of the User.
     * @param ip IPv4 Address of the User.
     * @return String response
     * @throws RemoteException
     */
    String createAccount(String loginName, String realName, String password, String ip) throws RemoteException;
    /**
     * Returns a String containing the results of querying the server database for a given loginName.
     * @param loginName the login-name that will be used in the query.
     * @return String queryResults
     * @throws RemoteException
     */
    String lookupByLoginName(String loginName) throws RemoteException;
    /**
     * Returns a String containg the results of querying the server database for a given UUID.
     * @param uuid the UUID that will be used in the query.
     * @return String queryResults
     * @throws RemoteException
     */
    String lookupByUUID(UUID uuid) throws RemoteException;
    /**
     * Find the User entry in the server database that has credentials matching the currentLoginName and password parameters
     *  and update that user's login-name with the given updatedLoginName parameter. A String is returned indicating whether
     *  the operation was successful or not.
     * @param currentLoginName the login-name of the account to be updated
     * @param updatedLoginName the new login-name to update the account with
     * @param password the password of the user account
     * @return String operationResult
     * @throws RemoteException
     */
    String changeLoginName(String currentLoginName, String updatedLoginName, String password) throws RemoteException;
    /**
     * Find and remove the User entry in the server database that has credentials matching the given
     *  loginName and password parameters. Returns a String indicating whether the operation was successful or not.
     * @param loginName The loginName of the User
     * @param password The password of the User
     * @return String operationResult
     * @throws RemoteException
     */
    String deleteUsername(String loginName, String password) throws RemoteException;
}