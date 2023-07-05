package client;

import org.apache.commons.cli.*;
import org.apache.commons.cli.OptionBuilder;

import java.awt.print.Printable;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.rmi.server.RMIClientSocketFactory;
import java.util.ArrayList;
import java.util.Arrays;


import static org.apache.commons.cli.Option.*;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.UUID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

import server.IdServer;
import shared.Id;

import javax.rmi.ssl.SslRMIClientSocketFactory;

/**
 * The IdClient class is the driver class for the client side of the IdClient program.
 *  * @author JMORTON
 *  * @author JCROWELL
 *  * @version 1.0 CS455 Spring 2022
 */
public class IdClient {

    private static final Option ARG_SERVER = new Option("s", "server", true, "Server address");
    private static final Option ARG_PORT = new Option("n", "numport", true, "port #");
    private static final Option ARG_CREATE = new Option("c", "create", true, "Create new account");
    private static final Option ARG_LOOKUP = new Option("l", "lookup", true, "Lookup user info by login");
    private static final Option ARG_REVERSELOOKUP = new Option("r", "reverselookup", true, "Lookup user info by UUID");
    private static final Option ARG_MODIFY = new Option("m", "modify", true, "Modify existing login");
    private static final Option ARG_DELETE = new Option("d", "delete", true, "Delete an existing user account");
    private static final Option ARG_GET = new Option("g", "get", true, "Get a set of user info");
    private static final Option ARG_PASSWORD = new Option("p", "password", true, "Provide password credential");
    private static Id server;


    private static String[] serverAddresses = {"172.20.0.11","172.20.0.12","172.20.0.13","172.20.0.14"};


    // NEEDS TO TAKE HOST AND PORT NUMBERS, AS SUCH, ARGS[0] AND [1] NEED TO BE
    // --SERVER/-S AND --NUMPORT/-N

    /**
     * Searched the RMI registry for an Id server object and sets this object's Id server to the server it finds.
     * @param hostName
     * @param portNumber
     */
    public void startClient(String hostName, int portNumber) {
        for(String ip: serverAddresses){
            try {
                System.out.println("Asking " +ip+ " for coordinator address");
                RMIClientSocketFactory rmiClientSocketFactory = new SslRMIClientSocketFactory();
                Registry registry = LocateRegistry.getRegistry(ip, portNumber, rmiClientSocketFactory);
                this.server = (Id) registry.lookup("IdServer");
                String coordIP = server.getCoordinatorIP();
                System.out.println(ip + " says coordinator address is " + coordIP);
                if (!ip.equals(coordIP)) {
                    this.server = (Id) LocateRegistry.getRegistry(coordIP, portNumber, rmiClientSocketFactory).lookup("IdServer");
                }
                break;
            } catch (RemoteException e) {
                System.out.println(ip + " is not responding.");
                if(ip.equals("172.20.0.14")){
                    System.out.println("Could not connect to any servers.");
                    System.exit(1);
                }
            } catch (NotBoundException e) {
                System.err.println("Error when starting client.");
                e.printStackTrace();
            }

        }

    }


    /**
     * Calls the getAllUUID() for this client's Id server and prints the response
     */
    public void getAllUUIDs() {
        String response = "";
        try {
            // Editing server calls to accept the timestamp of the client, for consistency
            print(server.getAllUUIDs());
        } catch (RemoteException e) {
            System.err.println("Error when getting all UUIDs");
            e.printStackTrace();
        }
    }

    /**
     * Calls the getAllLoginNames() method of this object's Id server and prints the response.
     */
    public void getAllLoginNames() {
        String response = "";
        try {
            System.out.println(server.getAllLoginNames());
        } catch (RemoteException e) {

            System.err.println("Error when getting all login names.");
            e.printStackTrace();
        }

    }

    /**
     * Calls the getAllUsers() method this object's Id server and prints the result.
     */
    public void getAllUsers() {
        String retVal = "";
        try {
            System.out.println(server.getAllUsers());

        } catch (RemoteException e) {
            System.err.println("Error when getting all users.");
            e.printStackTrace();
        }

    }

    /**
     * Calls the createAccount method of this object's Id server with the passed parameters as arguments
     * @param loginName
     * @param realName
     * @param password
     * @param ip
     */
    public void createAccount(String loginName, String realName, String password, String ip) {
        try {
            print(server.createAccount(loginName, realName, hashThePass(password), ip));

        } catch (RemoteException e) {

            System.err.println("Error when creating an account.");
            e.printStackTrace();
        }
    }

    /**
     * Call the lookupByLoginname of this object's Id server and prints the result.
     * @param loginName
     */
    public void lookupByLoginName(String loginName) {
        String retVal = "";
        try {
            System.out.println(server.lookupByLoginName(loginName));

        } catch (RemoteException e) {

            System.err.println("Error when looking up by login name.");
            e.printStackTrace();
        }

    }

    /**
     * Calls the lookupByUUID method of this objects Id server and prints the result.
     * @param userUUID
     */
    public void lookupByUUID(UUID userUUID) {
        String retVal = "";
        try {
            System.out.println(server.lookupByUUID(userUUID));

        } catch (RemoteException e) {

            System.err.println("Error when looking up by UUID.");
            e.printStackTrace();
        }


    }

    /**
     * Calls the changeLoginName() method of this object's Id server and prints the response.
     * @param currentLoginName
     * @param updatedLoginName
     * @param password
     */
    public void changeLoginName(String currentLoginName, String updatedLoginName, String password) {
        try {
            System.out.println(server.changeLoginName(currentLoginName, updatedLoginName, hashThePass(password)));

        } catch (RemoteException e) {
            System.err.println("Error when changing login names.");
            e.printStackTrace();
        }
    }

    /**
     * Utility macro to shorten System.out.println() calls
     * @param output
     */
    public static void print(String output) {
        System.out.println(output);
    }

    /**
     * Hashes the given password and returns the digest
     * @param password
     * @return
     */
    private static String hashThePass(String password) {
        MessageDigest md;
        String hash = "";
        try {
            // Hashes the password and puts the resulting digest into a String.
            md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = password.getBytes();
            md.reset();
            byte[] result = md.digest(bytes);
            for (int i = 0; i < result.length; i++)
                hash += result[i];
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hash;
    }

    /**
     * Calls the deleteUsername() method for this object's Id server.
     * @param loginName
     * @param password
     */
    public void deleteUsername(String loginName, String password) {
        try {
            print(server.deleteUsername(loginName, hashThePass(password)));

        } catch (RemoteException e) {
            System.err.println("Error in deleting user login.");
            e.printStackTrace();
        }
    }

    /**
     * Prints the valid command line arguments
     */
    private static void printUsage() {
        System.out.println("Inteded usage is:");
        System.out.println(
                "java IdClient -s/--server <server host name> [-n/--numport <port number>] <Option letter/command> <Option arguments>\n");
        System.out.println("Option letter/commands & arguments:");
        System.out.println("-c/--create <login name> [<real name>] [--password <password>]");
        System.out.println("-l/--lookup <login name>");
        System.out.println("-r/--reverse-lookup <UUID>");
        System.out.println("-m/--modify <old login> <new login> [--password <password>]");
        System.out.println("-d/--delete <login> [--password <password>]");
        System.out.println("-g/--get <users | uuids | all>");
        System.out.println("If not specified, numport will default to 1099");
        System.out.println("Options separated by | are mutually exclusive");
        print("Options enclosed with square brackets [] are not required");
        System.exit(0);
    }

    /**
     * Driver class for IdClient program
     * @param args
     */
    public static void main(String[] args) {
        IdClient client = new IdClient();
        int port = 1099;
        System.setProperty("javax.net.ssl.trustStore", "src/main/resources/Client_Truststore");
        System.setProperty("javax.net.ssl.trustStorePassword", "test123");
        System.setProperty("java.security.policy", "mysecurity.policy");

        // ARG SETUPS
        ARG_CREATE.setArgs(2);
        ARG_CREATE.setArgName("loginname");
        ARG_CREATE.setArgName("realname");


        ARG_SERVER.setArgs(1);
        ARG_SERVER.setOptionalArg(false);
        ARG_SERVER.setArgName("serverhost");
        ARG_SERVER.setRequired(true);

        ARG_PORT.setArgs(1);
        ARG_PORT.setArgName("port#");
        ARG_PORT.setRequired(false);

        ARG_PASSWORD.setArgs(1);
        ARG_PASSWORD.setOptionalArg(true);
        ARG_PASSWORD.setRequired(false);

        ARG_LOOKUP.setArgs(1);
        ARG_LOOKUP.setOptionalArg(false);

        ARG_REVERSELOOKUP.setArgs(1);
        ARG_REVERSELOOKUP.setOptionalArg(false);

        ARG_MODIFY.setArgs(2);
        ARG_MODIFY.setOptionalArg(false);

        ARG_DELETE.setOptionalArg(false);
        ARG_DELETE.setArgs(1);


        Options options = new Options();
        options.addOption(ARG_SERVER);
        options.addOption(ARG_PORT);
        options.addOption(ARG_CREATE);
        options.addOption(ARG_LOOKUP);
        options.addOption(ARG_REVERSELOOKUP);
        options.addOption(ARG_MODIFY);
        options.addOption(ARG_DELETE);
        options.addOption(ARG_GET);
        options.addOption(ARG_PASSWORD);

        CommandLineParser clp = new BasicParser();

        try {
            CommandLine cl = clp.parse(options, args);
            // Server is provided
            if (cl.hasOption(ARG_SERVER.getOpt())) {
                String hostname = cl.getOptionValue(ARG_SERVER.getOpt());

                if (cl.hasOption(ARG_PORT.getOpt())) {
                    // Then set the port num
                    port = Integer.parseInt(cl.getOptionValue(ARG_PORT.getOpt()));
                }
                client.startClient(hostname, port);
                // CREATE
                if (cl.hasOption(ARG_CREATE.getOpt())) {

                    String loginName;
                    String realName;
                    String password = (cl.hasOption(ARG_PASSWORD.getOpt())) ? cl.getOptionValue(ARG_PASSWORD.getOpt()) : "";
                    if (cl.getOptionValues(ARG_CREATE.getOpt()).length == 2) {
                        loginName = cl.getOptionValues(ARG_CREATE.getOpt())[0];
                        realName = cl.getOptionValues(ARG_CREATE.getOpt())[1];
                        // Call add user method
                        client.createAccount(loginName, realName, hashThePass(password), InetAddress.getLocalHost().getHostAddress());
                    } else {
                        loginName = cl.getOptionValues(ARG_CREATE.getOpt())[0];
                        client.createAccount(loginName, System.getProperty("user.name"), hashThePass(password), InetAddress.getLocalHost().getHostAddress());
                    }
                }

                // GET
                else if (cl.hasOption(ARG_LOOKUP.getOpt())) {
                    String loginName = cl.getOptionValue(ARG_LOOKUP.getOpt());
                    client.lookupByLoginName(loginName);
                } else if (cl.hasOption(ARG_REVERSELOOKUP.getOpt())) {
                    String uuid = cl.getOptionValue(ARG_REVERSELOOKUP.getOpt());
                    client.lookupByUUID(UUID.fromString(uuid));
                } else if (cl.hasOption(ARG_GET.getOpt())) {
                    String param = cl.getOptionValue(ARG_GET.getOpt());
                    switch (param) {
                        case "users":
                            client.getAllLoginNames();
                            break;
                        case "uuids":
                            client.getAllUUIDs();
                            break;
                        case "all":
                            client.getAllUsers();
                            break;
                        default:
                            printUsage();
                            break;
                    }

                }

                // MODIFY
                else if (cl.hasOption(ARG_MODIFY.getOpt())) {

                    String[] params = cl.getOptionValues(ARG_MODIFY.getOpt());
                    if (params.length == 2) {
                        String oldLogin = params[0];
                        String newLogin = params[1];
                        String password = (cl.hasOption(ARG_PASSWORD.getOpt())) ? cl.getOptionValue(ARG_PASSWORD.getOpt()) : "";
                        client.changeLoginName(oldLogin, newLogin, hashThePass(password));
                    } else printUsage();
                }

                // DELETE
                else if (cl.hasOption(ARG_DELETE.getOpt())) {

                    String[] params = cl.getOptionValues(ARG_DELETE.getOpt());
                    String loginName = params[0];
                    String password = (cl.hasOption(ARG_PASSWORD.getOpt())) ? cl.getOptionValue(ARG_PASSWORD.getOpt()) : "";
                    client.deleteUsername(loginName, hashThePass(password));


                }
            }

            // Else if they're missing server (print usage)
            else {
                printUsage();
            }
        } catch (org.apache.commons.cli.ParseException e) {
            printUsage();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


    }
}
