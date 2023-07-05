


//      OBSOLETE CLASS, THOUGH WE WERE TOO SUPERSTITIOUS TO DELETE IT

//package server;
//
//import java.io.FileOutputStream;
//import java.io.ObjectOutputStream;
//import java.rmi.RemoteException;
//import java.rmi.server.RMIClientSocketFactory;
//import java.rmi.server.RMIServerSocketFactory;
//import java.rmi.server.UnicastRemoteObject;
//import java.util.HashMap;
//import java.util.Timer;
//import java.util.TimerTask;
//import java.util.UUID;
//
//import javax.rmi.ssl.SslRMIClientSocketFactory;
//import javax.rmi.ssl.SslRMIServerSocketFactory;
//import shared.Id;
//
///**
// * The IdImpl class is used to implement a server struct using the Id interface.
// *  * @author JMORTON
// *  * @author JCROWELL
// *  * @version 1.0 CS455 Spring 2022
// */
//public class IdImpl implements Id, java.io.Serializable {
//    private HashMap<String, UUID> loginToUUIDMap;
//    private HashMap<UUID,User> userDatabase;
//    private boolean verboseEnabled = false;
//
//
//    public IdImpl(){
//        loginToUUIDMap = new HashMap<>();
//        userDatabase = new HashMap<>();
//        startTimerForDiskSave();
//        exportToRMI();
//    }
//    public IdImpl(boolean verberoseEnabled){
//        userDatabase = new HashMap<>();
//        this.verboseEnabled = verberoseEnabled;
//        startTimerForDiskSave();
//        exportToRMI();
//    }
//
//
//
//    /**
//     * Takes a snapshot of this object and saves it to the disk in the id.ser file.
//     */
//    public void saveServerToDisk() {
//        try {
//            FileOutputStream fileOut = new FileOutputStream("id.ser");
//            ObjectOutputStream out = new ObjectOutputStream(fileOut);
//            out.writeObject(this);
//            out.close();
//            if(verboseEnabled) System.out.println("Saved server state to disk.");
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//    }
//
//    /**
//     * Sets a timer that will call saveServerToDisk() every 5.5 minutes.
//     */
//    public void startTimerForDiskSave(){
//        Timer timer = new Timer();
//        TimerTask task = new TimerTask(){
//            public void run(){
//                saveServerToDisk();
//            }
//        };
//        timer.scheduleAtFixedRate(task, 330000, 330000);
//    }
//
//
//    /**
//     * Exports this object to RMI using SSL.
//     */
//    public void exportToRMI(){
//        // This method is needed to export the server again if it was restored from the disk.
//        try {
//            RMIClientSocketFactory rmiClientSocketFactory = new SslRMIClientSocketFactory();
//            RMIServerSocketFactory rmiServerSocketFactory = new SslRMIServerSocketFactory();
//            UnicastRemoteObject.exportObject(this, 0, rmiClientSocketFactory, rmiServerSocketFactory);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Sets whether or not debug statements will be printed out.
//     * @param verbose true: print statements, false: do not print statements
//     */
//    public void setPrintStatements(boolean verbose){
//        this.verboseEnabled = verbose;
//    }
//
//    @Override
//    public String getAllLoginNames() throws RemoteException {
//
//        if(verboseEnabled) System.out.println("> Executing getAllLoginNames().");
//        // Case 1: Database is empty so send a response letting them know.
//        if(loginToUUIDMap.keySet().isEmpty()) return "ID-Server: The database is empty.";
//
//        // Case 2: Database is not empty so send the names.
//        String retVal = "---------------------- LOGIN-NAMES ----------------------\n";
//        for(String loginName: loginToUUIDMap.keySet()){
//            retVal += " * " + loginName + "\n";
//        }
//        return retVal;
//    }
//
//
//
//    @Override
//    public String getAllUUIDs() throws RemoteException {
//        if(verboseEnabled) System.out.println("> Executing getAllUUIDs().");
//        // Case 1: Database is empty so send a response letting them know.
//        if(userDatabase.isEmpty()) return "ID-Server: The database is empty.";
//
//        // Case 2: Database is not empty so send the names.
//        String retVal = "---------------------- UUIDs ----------------------\n";
//        for(User user: userDatabase.values()){
//            retVal += " * " + user.getUUID() + "\n";
//        }
//        return retVal;
//    }
//
//    @Override
//    synchronized public String createAccount(String loginName, String realName, String password, String ip ) throws RemoteException {
//        if(verboseEnabled) System.out.println("> Executing createAccount().");
//        // Hash the login name to determine UUID
//        UUID uuid = UUID.randomUUID();
//        // Use the hash as the key for the user entry in the database table
//        boolean loginNameAvailable = !loginToUUIDMap.containsKey(loginName);
//
//        // Case 1: Login name *is not* already a key in database.
//        if(loginNameAvailable){
//            User user = new User(loginName, realName, password, ip, uuid);
//            loginToUUIDMap.put(loginName, uuid);
//            userDatabase.put(uuid, user);
//            saveServerToDisk();
//            return "ID-Server: Successfully added user to database." + user;
//        }
//
//        // Case 2: Login name *is* already a key in the database.
//        else return "ID-Server: Failed to add user to database. The name '" +loginName+"' is already in use.";
//    }
//
//
//
//    @Override
//    public String lookupByLoginName(String loginName) throws RemoteException {
//        if(verboseEnabled) System.out.println("> Executing lookupByLoginName().");
//        // Case 1: Database is empty.
//        if(userDatabase.isEmpty()) return "ID-Server: The database is empty.";
//
//        // Case 2: Database is not empty and an entry exists for the loginName.
//        if(loginToUUIDMap.containsKey(loginName)){
//            UUID uuid = loginToUUIDMap.get(loginName);
//            User user = userDatabase.get(uuid);
//            if(user != null) return "ID-Server: " + user;
//        }
//
//
//        // Case 3: Database is not empty and no entry exists for the loginName.
//        return "ID-Server: No record for '" + loginName +"' found.";
//    }
//
//
//    @Override
//    public String lookupByUUID(UUID userUUID) throws RemoteException {
//        if(verboseEnabled) System.out.println("> Executing lookupByUUID().");
//        // Case 1: Database is empty.
//        if(userDatabase.isEmpty()) return "ID-Server: The database is empty.";
//
//        // Case 2: Database is not empty and user exists
//        User user = userDatabase.get(userUUID);
//        if(user != null) return "ID-Server: " + user;
//
//            // Case 3: Database is not empty but user does not exist
//        else return "ID-Server: Unable to locate user with that UUID in the database.";
//    }
//
//
//    @Override
//    synchronized public String changeLoginName(String currentLoginName, String updatedLoginName, String password) throws RemoteException {
//        if(verboseEnabled) System.out.println("> Executing changeLoginName().");
//        // Case: Database is empty.
//        if(userDatabase.isEmpty()) return "ID-Server: The database is empty.";
//        // Case: The desired new login name is already in use
//        if(loginToUUIDMap.containsKey(updatedLoginName)) return "ID-Server: Failed to update database. Name '" + updatedLoginName + "' has already been claimed.";
//        // Case: User does not exist
//        if(!loginToUUIDMap.containsKey(currentLoginName)) return  "ID-Server: Failed to update database. User not found.";
//
//        // Case 2: Database is not empty
//        // The login name exists and corresponds to a UUID
//        UUID uuid = loginToUUIDMap.get(currentLoginName);
//            // The actual user db contains that UUID key and corresponds to a User
//        if(userDatabase.containsKey(uuid)){
//            User user = userDatabase.get(uuid);
//            // Password is valid so change their login name
//            if(user.passTheHash().equals(password)){
//                // Set their login name
//                user.setLoginName(updatedLoginName);
//                // Remove that login name from the login to uuid map
//                loginToUUIDMap.remove(currentLoginName);
//                // Map the updated login name to the User's UUID.
//                loginToUUIDMap.put(updatedLoginName, uuid);
//                saveServerToDisk();
//                return "ID-Server: Database successfully updated. Name changed from '"+currentLoginName+"' ---> '"+updatedLoginName+"'";
//            }
//            // Password invalid
//            else return "ID-Server: Failed to change login name. Incorrect password.";
//        }
//        return "ID-Server: Failed to change login name.";
//    }
//
//
//    @Override
//    synchronized public String deleteUsername(String loginName, String passHash) throws RemoteException {
//        if(verboseEnabled) System.out.println("> Executing deleteUsername().");
//        // Case: Database is empty.
//        if(userDatabase.isEmpty()) return "ID-Server: The database is empty.";
//        // Case: User not found.
//        if(!loginToUUIDMap.containsKey(loginName)) return "ID-Server: User not found.";
//
//        // Case 2: Database is not empty and has key
//        UUID uuid = loginToUUIDMap.get(loginName);
//        User user = userDatabase.get(uuid);
//        System.out.println(user.passTheHash());
//        System.out.println(passHash);
//        if(user != null){
//            // Case 2a: User provided the right password
//            if(user.passTheHash().equals(passHash)){
//
//                loginToUUIDMap.remove(loginName);
//                userDatabase.remove(uuid);
//                saveServerToDisk();
//                return "ID-Server: Successfully deleted user from database.";
//            }
//            // Case 2b: User provided the wrong password
//            return "ID-Server: Failed to delete user from the database. Invalid password.";
//        }
//        // Case 3: Black magic.
//        return "ID-Server: Failed to delete user from the database.";
//    }
//
//
//    @Override
//    public String getAllUsers() throws RemoteException {
//        if(verboseEnabled) System.out.println("> Executing getAllUsers().");
//        // Case 1: Database is empty.
//        if(userDatabase.isEmpty()) return "ID-Server: The database is empty.";
//
//        // Case 2: Database is not empty.
//        String retVal = "ID-Server: \n ---------------------------------------- User List -----------------------------------------";
//        for(User user: userDatabase.values()){
//            retVal+= user.toString();
//            retVal+=" --------------------------------------------------------------------------------------------\n";
//        }
//        String testVal  = "";
//        testVal = retVal.replaceAll("\\]|\\[|\\,", "");
//        return testVal;
//    }
//
//    public static void main(String args[]){
//
//    }
//}