package server;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;

/**
 * The Database Class is a representation of all the data of the ID server that is locally maintained on
 * any one server, Serialized so as to send out to other members of the cluster for the purposes of replication
 */
public class Database implements Serializable {
    private HashMap<UUID, User> database;
    private HashMap<String, UUID> loginToUUIDMap;
    private HashMap<Integer, DatabaseOperation> ledger;
    private Integer timestamp;

    /**
     * Database constructor, default
     */
    public Database(){
        this.database = new HashMap<>();
        this.ledger = new HashMap<>();
        this.loginToUUIDMap = new HashMap<>();
        timestamp = 0;
    }

    /**
     * Database overloaded constructor
     * @param database The list of users and their UUIDs on the local ID server
     * @param ledger The list of operations performed on the Database, along with timestamp
     */
    public Database(HashMap<UUID, User> database, HashMap<Integer, DatabaseOperation> ledger){
        this.database = database;
        this.ledger = ledger;
        this.loginToUUIDMap = new HashMap<>();
        timestamp = 0;
    }

    /**
     * Method to ensure a login name chosen by a Client is available in the database
     * @param loginName The login name whose availability is being checked
     * @return Boolean, True if the name has not been used, False otherwise
     */
    public boolean checkNameAvailability(String loginName){
        boolean retVal = true;
        for(String name : loginToUUIDMap.keySet()){
            if(name.equals(loginName)){
                retVal = false;
                break;
            }
        }
        return retVal;
    }

    /**
     * Method to create a new user in the ID Server
     * @param operation A DatabaseCreate Object that contains the data to be added to the ID Server
     * @return String stating result of operation: success or failure
     */
    public String createUser(DatabaseCreate operation){
        // Use the hash as the key for the user entry in the database table
        boolean loginNameAvailable = checkNameAvailability(operation.getLoginName());
        // Case 1: Login name *is not* already a key in database.
        if (loginNameAvailable) {
            User user = operation.getUser();
            loginToUUIDMap.put(user.getLoginName(), user.getUUID());
            database.put(user.getUUID(), user);
            updateLedger(operation);
            return "ID-Server: Successfully added user to database. \nTimestamp:"+timestamp +"\n"+ user;
        }
        return "ID-Server: Failed to add user to database";
    }

    /**
     * Method to delete a user from the ID Server
     * @param operation A DatabaseDelete Object that contains the data to be removed from the ID Server
     * @return String stating result of operation: success or failure
     */
    public String deleteUser(DatabaseDelete operation){
        if (!loginToUUIDMap.containsKey(operation.getLoginName())) return "ID-Server: User not found.";

        // Case 2: Database is not empty and has key
        UUID uuid = loginToUUIDMap.get(operation.getLoginName());
        User user = database.get(uuid);
        System.out.println(user.passTheHash());
        if (user != null) {
            // Case 2a: User provided the right password
            if (user.passTheHash().equals(operation.getPassword())) {
                loginToUUIDMap.remove(operation.getLoginName());
                database.remove(uuid);
                updateLedger(operation);
                return "ID-Server: Successfully deleted user from database. \nTimestamp: " +timestamp;
            }
            // Case 2b: User provided the wrong password
            return "ID-Server: Failed to delete user from the database. Invalid password.";
        }
        // Case 3: Black magic.
        return "ID-Server: Failed to delete user from the database.";
    }

    /**
     * Obtains the current timestamp of the Database
     * @return Integer - the timestamp of the next operation that will be called
     */
    public Integer getTimestamp(){return this.timestamp;}

    /**
     * Method to modify the data of a user on the Database
     * @param operation A DatabaseUpdate Object that contains the data to be modified, and the data to modify it to
     * @return String stating result of operation: success or failure
     */
    public String updateDatabase(DatabaseUpdate operation){
        if (loginToUUIDMap.containsKey(operation.getUpdatedLoginName()))
            return "ID-Server: Failed to update database. Name '" + operation.getUpdatedLoginName() + "' has already been claimed.";
        // Case: User does not exist
        if (!loginToUUIDMap.containsKey(operation.getLoginName()))
            return "ID-Server: Failed to update database. User not found.";

        // Case 2: Database is not empty
        // The login name exists and corresponds to a UUID
        UUID uuid = loginToUUIDMap.get(operation.getLoginName());
        // The actual user db contains that UUID key and corresponds to a User
        if (database.containsKey(uuid)) {
            User user = database.get(uuid);
            // Password is valid so change their login name
            if (user.passTheHash().equals(operation.getPassword())) {
                // Set their login name
                user.setLoginName(operation.getUpdatedLoginName());
                // Remove that login name from the login to uuid map
                loginToUUIDMap.remove(operation.getLoginName());
                // Map the updated login name to the User's UUID.
                loginToUUIDMap.put(operation.getUpdatedLoginName(), uuid);
                updateLedger(operation);
                return "ID-Server: Database successfully updated. Name changed from '" + operation.getLoginName() + "' ---> '" + operation.getUpdatedLoginName() + "'";
            }
            // Password invalid
            else return "ID-Server: Failed to change login name. Incorrect password.";
        }
        return "ID-Server: Failed to change login name.";
    }

    /**
     * Method to query Database to confirm existence of a requested user by login name
     * @param loginName String of the username the Database is to look for
     * @return String stating result of operation: success or failure
     */
    public String getUser(String loginName){
        // Case 1: Database is empty.
        if (database.isEmpty()) return "ID-Server: The database is empty.";

        // Case 2: Database is not empty and an entry exists for the loginName.
        if (loginToUUIDMap.containsKey(loginName)) {
            UUID uuid = loginToUUIDMap.get(loginName);
            User user = database.get(uuid);
            if (user != null) return "ID-Server: " + user;
        }


        // Case 3: Database is not empty and no entry exists for the loginName.
        return "ID-Server: No record for '" + loginName + "' found.";
    }

    /**
     * Method to query Database to confirm existence of a requested user by UUID
     * @param uuid UUID of the user the Database is to look for
     * @return String stating result of operation: success or failure
     */
    public String getUser(UUID uuid){
        // Case 1: Database is empty.
        if (database.isEmpty()) return "ID-Server: The database is empty.";

        // Case 2: Database is not empty and user exists
        User user = database.get(uuid);
        if (user != null) return "ID-Server: " + user;

            // Case 3: Database is not empty but user does not exist
        else return "ID-Server: Unable to locate user with that UUID in the database.";
    }

    /**
     * Method to query Database to list all users contained on it by both username and UUID
     * @return Strings representing every user currently entered in the Database, both username and UUID
     */
    public String getAllUsers(){
        // Case 1: Database is empty.
        if (database.isEmpty()) return "ID-Server: The database is empty.";

        // Case 2: Database is not empty.
        String retVal = "ID-Server: \n ---------------------------------------- User List -----------------------------------------";
        for (User user : database.values()) {
            retVal += user.toString();
            retVal += " --------------------------------------------------------------------------------------------\n";
        }
        String testVal = "";
        testVal = retVal.replaceAll("\\]|\\[|\\,", "");
        return testVal;
    }

    /**
     * Method to query Database to list all users contained within it by UUID
     * @return UUIDs of every user currently entered in the Database
     */
    public String getAllUUIDs(){
        // Case 1: Database is empty so send a response letting them know.
        if (database.isEmpty()) return "ID-Server: The database is empty.";

        // Case 2: Database is not empty so send the names.
        String retVal = "---------------------- UUIDs ----------------------\n";
        for (User user : database.values()) {
            retVal += " * " + user.getUUID() + "\n";
        }
        return retVal;
    }

    /**
     * Method to query Database to list all users contained within it by username Strings
     * @return String usernames of every user currently entered in the Database
     */
    public String getAllLoginNames(){
        // Case 1: Database is empty so send a response letting them know.
        if (loginToUUIDMap.keySet().isEmpty()) return "ID-Server: The database is empty.";
        // Case 2: Database is not empty so send the names.
        String retVal = "---------------------- LOGIN-NAMES ----------------------\n";
        for (String loginName : loginToUUIDMap.keySet()) {
            retVal += " * " + loginName + "\n";
        }
        return retVal;
    }

    /**
     * Method to obtain the ledger listing all operations and their timestamps
     * @return HashMap of the database's ledger
     */
    public HashMap<Integer, DatabaseOperation> getLedger(){
        return this.ledger;
    }

    /**
     * Method to enter a new operation into the ledger of the database and increment the database's timestamp
     * @param operation DatabaseOperation Object containing instructions and current timestamp
     */
    private void updateLedger(DatabaseOperation operation){
        // Update ledger.
        this.ledger.put(operation.getTimestamp(), operation);
        this.timestamp++;
        saveToDisk();

    }

    /**
     * Method to save the current state of the ID Server's ledger to the local hard disk
     */
    public void saveToDisk(){
        // Save ledger to disk.
        try {
            FileOutputStream fileOut = new FileOutputStream("db-ledger");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this);
            out.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }


}
