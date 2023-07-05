package server;

import java.io.*;
import java.net.*;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;


import org.apache.commons.cli.*;
import shared.Id;
import shared.Utility;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

/**
 * The IdServer class is used as a driver class for the server.
 *
 * @author JMORTON
 * @author JCROWELL
 * @version 1.0 CS455 Spring 2022
 */
public class IdServer extends UnicastRemoteObject implements Id, java.io.Serializable, IdServerMember {
    private static final Option ARG_VERB = new Option("v", "verbose", false, "Verbose mode for server yes/no");
    private static final Option ARG_PORT = new Option("n", "numport", true, "Specify a port; default is 1099");
    private static final Option ARG_IP = new Option("i", "iplist", true, "List of server IPs; first in list is this server's");
    private static final Option ARG_NIC = new Option("c", "netcard", true, "Name of network interface card");
    // Network Variables
    ServerSocket servSock;
    Socket sendSock;
    private long myPID;
    private String myIP;
    private String nextIP;
    private int port;
    private String coordinatorIP;
    private Long coordinatorPID;
    private String serverOne = "172.20.0.11";
    private String serverTwo = "172.20.0.12";
    private String serverThree = "172.20.0.13";
    private String serverFour = "172.20.0.14";
    private String[] servers;
    private transient Timer tim = new Timer();
    private transient TimerTask task;
    private boolean electionInProgress = false;
    private int timestamp;
    private Database database;
    private boolean verboseEnabled = false;
    private Object lock = new Object();
    private Ballot ballot;

    /**
     * Constructor for IdServer Object
     *
     * @throws RemoteException
     */
    public IdServer() throws RemoteException {
        super();
        this.database = restoreDB();
        startTimerForDiskSave();
    }

    /**
     * Overloaded Constructor
     *
     * @param verberoseEnabled Boolean Sets verbose mode
     * @throws RemoteException
     */
    public IdServer(boolean verberoseEnabled) throws RemoteException {
        super();
        this.verboseEnabled = verberoseEnabled;
        this.database = restoreDB();
        startTimerForDiskSave();
    }

    /**
     * Driver method
     *
     * @param args String array CLI aruments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // SSL Setup
        System.setProperty("javax.net.ssl.keyStore", "src/main/resources/Server_Keystore");
        System.setProperty("javax.net.ssl.keyStorePassword", "test123");
        System.setProperty("java.security.policy", "mysecurity.policy");
        System.setProperty("javax.net.ssl.trustStore", "src/main/resources/Client_Truststore");
        System.setProperty("javax.net.ssl.trustStorePassword", "test123");
        System.setProperty("sun.rmi.transport.proxy.connectTimeout", "2000");
        System.setProperty("sun.rmi.transport.tcp.responseTimeout", "2000");
        System.setProperty("sun.rmi.transport.tcp.readTimeout", "2000");
        System.setProperty("sun.rmi.transport.tcp.handshakeTimeout", "2000");
        System.setProperty("sun.rmi.transport.connectionTimeout", "2000");


        // RMI port specification
        ARG_PORT.setArgs(1);
        ARG_PORT.setArgName("portnumber");
        ARG_PORT.setOptionalArg(true);
        ARG_PORT.setRequired(false);

        // Verbosity
        boolean verboseFlag = false;

        // Default RMI port
        int servePort = 1099;


        Options options = new Options();
        options.addOption(ARG_VERB);
        options.addOption(ARG_PORT);

        CommandLineParser clp = new BasicParser();

        try {
            CommandLine cl = clp.parse(options, args);

            // Handle possible verbose flag
            if (cl.hasOption(ARG_VERB.getOpt())) {
                verboseFlag = true;
            }

            // Handle possible port flag
            if (cl.hasOption(ARG_PORT.getOpt())) {
                servePort = Integer.parseInt(cl.getOptionValue(ARG_PORT.getOpt()));
            }

            // Get the server from disk or create a new one.
            final IdServer server = new IdServer();
            server.setPID(ProcessHandle.current().pid() + new Random().nextInt(100));
            server.setIP(InetAddress.getLocalHost().getHostAddress());
            server.setNextIP();

            server.setPort(servePort);

            System.out.println(NetworkInterface.getNetworkInterfaces());
            // Registry Binding
            RMIClientSocketFactory rmiClientSocketFactory = new SslRMIClientSocketFactory();
            RMIServerSocketFactory rmiServerSocketFactory = new SslRMIServerSocketFactory();
            Registry registry = LocateRegistry.createRegistry(servePort, rmiClientSocketFactory, rmiServerSocketFactory);
            try {
                registry.bind("IdServer", server);

            } catch (AlreadyBoundException e) {
                throw new RuntimeException(e);
            }


            RMIClientSocketFactory rmiClientSocketFact = new RMISocketFactory() {
                public Socket createSocket(String host, int port) throws IOException {
                    Socket socket = new TimeoutSocket(host, port);
                    socket.setSoTimeout(2000);
                    socket.setSoLinger(false, 0);
                    return socket;
                }

                public ServerSocket createServerSocket(int port) throws IOException {
                    return new ServerSocket(port);
                }

                class TimeoutSocket extends Socket {
                    public TimeoutSocket(String host, int port) throws IOException {
                        super(host, port);
                    }

                    @Override
                    public void connect(SocketAddress endpoint) throws IOException {
                        connect(endpoint, 2000);
                    }
                }
            };
            RMIServerSocketFactory rmiServerSocketFactory1 = new RMISocketFactory() {
                public Socket createSocket(String host, int port) throws IOException {
                    Socket socket = new TimeoutSocket(host, port);
                    socket.setSoTimeout(2000);
                    socket.setSoLinger(false, 0);
                    return socket;
                }

                public ServerSocket createServerSocket(int port) throws IOException {
                    return new ServerSocket(port);
                }

                class TimeoutSocket extends Socket {
                    public TimeoutSocket(String host, int port) throws IOException {
                        super(host, port);
                    }

                    @Override
                    public void connect(SocketAddress endpoint) throws IOException {
                        connect(endpoint, 2000);
                    }
                }
            };
            Registry serverReg = LocateRegistry.createRegistry(5005, rmiClientSocketFact, rmiServerSocketFactory1);
            serverReg.bind("IdServerMember", server);

            switch (server.myIP) {
                case "172.20.0.11":
                    server.serverOne = "172.20.0.12";
                    server.serverTwo = "172.20.0.13";
                    server.serverThree = "172.20.0.14";
                    break;
                case "172.20.0.12":
                    server.serverOne = "172.20.0.13";
                    server.serverTwo = "172.20.0.14";
                    server.serverThree = "172.20.0.11";
                    break;
                case "172.20.0.13":
                    server.serverOne = "172.20.0.14";
                    server.serverTwo = "172.20.0.11";
                    server.serverThree = "172.20.0.12";
                    break;
                case "172.20.0.14":
                    server.serverOne = "172.20.0.11";
                    server.serverTwo = "172.20.0.12";
                    server.serverThree = "172.20.0.13";
            }
            server.servers = new String[]{server.serverOne, server.serverTwo, server.serverThree};
            server.synchronizeDB();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        synchronized (server.lock) {
                            try {
                                server.lock.wait();
                                server.sendBallot();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }).start();

            server.startElection();
            server.setPrintStatements(verboseFlag);


            // Set Timer For Disk Save
            server.startTimerForDiskSave();
            // Adds shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread() {

                public void run() {
                    System.out.println("\nShutting down...");
                    System.out.println("Saving server to disk.");
                    server.database.saveToDisk();
                }
            });

        } catch (ParseException e) {
            System.out.println("PARSE EXCEPTION");

        } catch (UnknownHostException e) {
            System.out.println("UNKNOWN HOST EXCEPTION");
        }

        // Should be last exception due to inheritance?
        catch (IOException e) {
            System.out.println("GENERALIZED IO EXCEPTION -_-");
        } catch (AlreadyBoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Prints the CLI usage of the IdServer program.
     */
    public static void printUsage() {
        System.err.println("java IdServer [--numport <port#>] [--verbose]");
    }

    private void sendBallot() {

        for (String server : servers) {
            try {
                System.out.println("Attempting to pass ballot to " + server);
                getServerFromIp(server).receiveBallot(this.getBallot());
                return;
            } catch (RemoteException | NotBoundException e) {
                System.out.println("Failed to send ballot to " + server);
            }
        }
        System.out.println("All servers are down");


    }

    public void receiveBallot(Ballot ballot) throws RemoteException {
        if (!ballot.getAuthorIP().equals(myIP)) {
            this.ballot = ballot;
            this.ballot.addToCandidates(new Candidate(myIP, myPID));
            synchronized (lock) {
                lock.notifyAll();
            }

        } else {
            coordinator(myIP, ballot.electCoordinator());
        }
    }

    private Database restoreDB() {
        if (new File("db-ledger").exists()) {
            try {
                // Attempt to retrieve the file containing the previous server object.
                FileInputStream fileIn = null;
                try {
                    fileIn = new FileInputStream("db-ledger");
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                ObjectInputStream in = new ObjectInputStream(fileIn);

                // Attempt to read in the object from the file and return it.
                Object obj = in.readObject();
                in.close();
                System.out.println("Restoring server from previous state.");
                Database db = (Database) obj;
                return db;
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            return new Database();
        }
    }

    /**
     * Sets a timer that will call saveServerToDisk() every 5.5 minutes.
     */
    public void startTimerForDiskSave() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                database.saveToDisk();
            }
        };
        timer.scheduleAtFixedRate(task, 330000, 330000);
    }

    /**
     * Sets whether or not debug statements will be printed out.
     *
     * @param verbose true: print statements, false: do not print statements
     */
    public void setPrintStatements(boolean verbose) {
        this.verboseEnabled = verbose;
    }

    private void setPort(int port) {
        this.port = port;
    }

    @Override
    /**
     * Obtains timestamp of server
     */
    public Integer getLamportTimestamp() throws RemoteException {
        return timestamp;
    }

    /**
     * Checks for existing servers to sync to at startup
     */
    public void synchronizeDB() {
        for (String server : servers) {
            try {
                System.out.println("Attempting to query " + server + " for DB sync");
                Id member = (Id) getServerFromIp(server);
                IdServerMember coord = getServerFromIp(member.getCoordinatorIP());
                this.database = coord.getDB();
                return;
            } catch (RemoteException | NotBoundException e) {

            }
        }
        System.out.println("Restoring from backup.");
    }

    /**
     * Sends passed Operation to all other servers in the cluster
     *
     * @param operation DatabaseOperation Object to be replicated
     */
    synchronized private void replicateToAll(DatabaseOperation operation) {
        if (coordinatorIP.equals(myIP)) {
            for (String server : servers) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getServerFromIp(server).replicateWrite(operation);
                            System.out.println("Replicated write to " + server);
                        } catch (RemoteException | NotBoundException e) {
                            System.out.println("Failed to write to " + server);
                        }
                    }
                }).start();
            }

        }
    }

    @Override
    synchronized public void replicateWrite(DatabaseOperation operation) throws RemoteException {
        if (operation instanceof DatabaseCreate) {
            if (database.getTimestamp() != operation.getTimestamp() - 1) {
                try {
                    this.database = getServerFromIp(getCoordinatorIP()).getDB();
                } catch (NotBoundException e) {
                    System.out.println("error in replicateWrite");
                }
            } else {
                this.database.createUser((DatabaseCreate) operation);
            }
        }
        if (operation instanceof DatabaseUpdate) {
            if (database.getTimestamp() != operation.getTimestamp() - 1) {
                try {
                    this.database = getServerFromIp(getCoordinatorIP()).getDB();
                } catch (NotBoundException e) {
                    System.out.println("error in replicateWrite");
                }
            } else {
                this.database.updateDatabase((DatabaseUpdate) operation);
            }
        }
        if (operation instanceof DatabaseDelete) {
            if (database.getTimestamp() != operation.getTimestamp() - 1) {
                try {
                    this.database = getServerFromIp(getCoordinatorIP()).getDB();
                } catch (NotBoundException e) {
                    System.out.println("error in replicateWrite");
                }
            } else {
                this.database.deleteUser((DatabaseDelete) operation);
            }
        }

    }

    /**
     * Method to obtain current Database
     *
     * @return Database object representing the ID Server database
     */
    public Database getDB() {
        return this.database;
    }

    /**
     * Method to obtain the Coordinator's ledger, showing operations by timestamp
     *
     * @return HashMap of Coordinator ledger
     * @throws RemoteException
     */
    public HashMap<Integer, DatabaseOperation> getCoordinatorLedger() throws RemoteException {
        if (coordinatorIP.equals(myIP)) {
            return database.getLedger();
        }
        return null;
    }

    /**
     * Gets the IP of the current Coordinator
     *
     * @return String IP of current Coordinator
     */
    public String getCoordinatorIP() {
        return this.coordinatorIP;
    }

    synchronized public void setCoordinatorIP(String coordinatorIP) {
        this.coordinatorIP = coordinatorIP;
        if (!coordinatorIP.equals(myIP)) checkForPulse();
        System.out.println("Coordinator has been set to " + coordinatorIP);
    }

    private String getIP() {
        return this.myIP;
    }

    private void setIP(String ip) {
        this.myIP = ip;
    }

    public Long getPID() {
        return this.myPID;
    }

    private void setPID(long pid) {
        this.myPID = pid;
    }

    @Override
    public String getAllLoginNames() throws RemoteException {
        if (getCoordinatorIP().equals(myIP)) {
            return this.database.getAllLoginNames();
        }
        return "I am not the coordinator.... can't give you that.";
    }

    @Override
    public String getAllUUIDs() throws RemoteException {
        if (getCoordinatorIP().equals(myIP)) {
            return this.database.getAllUUIDs();
        }
        return "I am not the coordinator... can't give you that.";
    }

    @Override
    synchronized public String createAccount(String loginName, String realName, String password, String ip) throws RemoteException {
        if (getCoordinatorIP().equals(myIP)) {
            if (verboseEnabled) System.out.println("> Executing createAccount().");
            // Hash the login name to determine UUID
            UUID uuid = UUID.randomUUID();
            DatabaseCreate op = new DatabaseCreate(database.getTimestamp(), loginName, realName, password, ip, uuid);
            String dbResponse = database.createUser(op);
            if (!dbResponse.contains("ID-Server: Failed to add user to database")) {
                replicateToAll(op);
            }
            return dbResponse;
        }
        return "I am not the coordinator... I can't do that.";
    }

    public String lookupByLoginName(String loginName) throws RemoteException {
        if (getCoordinatorIP().equals(myIP)) {
            return database.getUser(loginName);
        }
        return "I am not the coordinator... I can't do that.";
    }

    public String lookupByUUID(UUID userUUID) throws RemoteException {
        if (getCoordinatorIP().equals(myIP)) {
            return database.getUser(userUUID);
        }
        return "I am not the coordinator... I can't do that";
    }

    synchronized public String changeLoginName(String currentLoginName, String updatedLoginName, String password) throws RemoteException {
        if (getCoordinatorIP().equals(myIP)) {
            UUID uuid = UUID.randomUUID();
            DatabaseUpdate operation = new DatabaseUpdate(database.getTimestamp(), currentLoginName, updatedLoginName, password);
            String dbResponse = database.updateDatabase(operation);
            if (!dbResponse.contains("ID-Server: Failed")) {
                replicateToAll(operation);
            }
            return dbResponse;
        }
        return "I am not the coordinator... I can't do that.";
    }

    synchronized public String deleteUsername(String loginName, String passHash) throws RemoteException {
        if (getCoordinatorIP().equals(myIP)) {
            DatabaseDelete op = new DatabaseDelete(database.getTimestamp(), loginName, passHash);
            String dbResponse = database.deleteUser(op);
            if (!dbResponse.contains("ID-Server: Failed") && !dbResponse.equals("ID-Server: User not found.")) {
                replicateToAll(op);
            }
            return dbResponse;
        }
        return "I am not the coordinator... I can't do that";
    }

    public String getAllUsers() throws RemoteException {
        if (getCoordinatorIP().equals(myIP)) {
            return this.database.getAllLoginNames();
        }
        return "I am not the coordinator... I can't do that";
    }

    /**
     * Method to obtain IP and PID of this server
     *
     * @return String the IP and PID of this server
     */
    public String toString() {
        String retVal = "IP: " + this.myIP + "\n";
        retVal += "PID: " + this.myPID + "\n";
        return retVal;
    }

    /**
     * Method to obtain an IdServer Object from a given IP String
     *
     * @param ip String of IP of intended IdServer
     * @return IdServer Object of IdServer
     * @throws RemoteException
     * @throws NotBoundException
     */
    private IdServerMember getServerFromIp(String ip) throws RemoteException, NotBoundException {
        RMIClientSocketFactory rmiClientSocketFactory = new SslRMIClientSocketFactory();
        return (IdServerMember) LocateRegistry.getRegistry(ip, port, rmiClientSocketFactory).lookup("IdServer");
    }

    /**
     * Method to obtain an IdServer Object from a given IP String, without SSL Encryption
     *
     * @param ip String of IP of intended IdServer
     * @return IdServer Object of IdServer
     * @throws RemoteException
     * @throws NotBoundException
     */
    private IdServerMember getServerFromIpNoCrypto(String ip) throws RemoteException, NotBoundException {
        RMIClientSocketFactory rmiClientSocketFact = new RMISocketFactory() {
            public Socket createSocket(String host, int port) throws IOException {
                Socket socket = new TimeoutSocket(host, port);
                socket.setSoTimeout(2000);
                socket.setSoLinger(false, 0);
                return socket;
            }

            public ServerSocket createServerSocket(int port) throws IOException {
                return new ServerSocket(port);
            }

            class TimeoutSocket extends Socket {
                public TimeoutSocket(String host, int port) throws IOException {
                    super(host, port);
                }

                @Override
                public void connect(SocketAddress endpoint) throws IOException {
                    connect(endpoint, 2000);
                }
            }
        };
        return (IdServerMember) LocateRegistry.getRegistry(ip, 5005, rmiClientSocketFact).lookup("IdServerMember");
    }

    /**
     * Method to obtain a Ballot from a Server during an election
     *
     * @return Ballot object Ballot of the server
     */
    private Ballot getBallot() {
        return this.ballot;
    }

    /**
     * Method to begin an election amongst the cluster
     */
    private void startElection() {
        System.out.println("Starting Election");

        Ballot ballot = null;
        try {
            ballot = new Ballot(myIP, myPID);
            System.out.println("BALLOT BOX: " + ballot);
        } catch (RemoteException e) {
            System.out.println("Trouble making ball");
        }
        for (String server : servers) {
            try {
                System.out.println("Attempting to pass ballot to " + server);
                getServerFromIpNoCrypto(server).receiveBallot(ballot);
                return;

            } catch (RemoteException | NotBoundException e) {

            }
        }
        System.out.println("nobody responded so I am coord");
        setCoordinatorIP(myIP);
    }


    /**
     * Method to replicate election results across the cluster to confirm consensus
     *
     * @param authorIp             IP of the Server sending the election results
     * @param coordinatorIpAddress IP of the Coordinator elected by the results
     * @throws RemoteException
     */
    public void coordinator(String authorIp, String coordinatorIpAddress) throws RemoteException {

        setCoordinatorIP(coordinatorIpAddress);
        for (String server : servers) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println("Attempting to notify " + server + " of election results");
                        getServerFromIpNoCrypto(server).setCoordinatorIP(coordinatorIpAddress);
                    } catch (RemoteException | NotBoundException e) {
                        System.out.println("Failed to update coordinator at server: " + server);
                    }
                }
            }).start();
        }

    }

    /**
     * Utility method to set the Ring of Servers
     */
    private void setNextIP() {
        if (myIP.equals("172.20.0.11")) {
            nextIP = "172.20.0.12";
        }
        if (myIP.equals("172.20.0.12")) {
            nextIP = "172.20.0.13";
        }
        if (myIP.equals("172.20.0.13")) {
            nextIP = "172.20.0.14";
        }
        if (myIP.equals("172.20.0.14")) {
            nextIP = "172.20.0.11";
        }
    }

    /**
     * Method for a Server to state its Coordinator (Unused)
     */
    private void whoIsCoord() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.println("THIS IS COORDINATOR: " + getCoordinatorIP());
            }
        };
        timer.scheduleAtFixedRate(task, 3000, 3000);
    }

    /**
     * Timer method for Server to contact the current Coordinator to ensure it is still up
     */
    private void checkForPulse() {
        if (this.tim != null) {
            this.tim.cancel();
            this.tim.purge();
        }
        this.tim = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    System.out.println("Attempting to ping coordinator");
                    IdServerMember member = getServerFromIpNoCrypto(getCoordinatorIP());
                    System.out.println(member.heartbeat());
                } catch (RemoteException | NotBoundException e) {
                    System.out.println(getCoordinatorIP() + " is not responding.");
                    startElection();
                    tim.cancel();
                    tim.purge();
                }
            }
        };
        tim.scheduleAtFixedRate(task, 0, 5000 + (long) myPID);

    }

    /**
     * Method for Coordinator to confirm its continued existence in the cluster
     *
     * @return String confirming Coordinator is still up
     * @throws RemoteException
     */
    public String heartbeat() throws RemoteException {
        return myIP + " " + myPID + " says hello!";
    }

}



