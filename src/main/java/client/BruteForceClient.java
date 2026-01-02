package client;

import common.BruteForceService;
import common.SearchConfig;
import common.SearchResult;
import server.RMIServer;
import util.MD5Util;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.CompletionService;

/**
 * BruteForceClient - Command-line client for distributed MD5 brute-force
 * Connects to RMI servers and coordinates the distributed search
 */
public class BruteForceClient {
    
    private String[] serverHosts;  // Array of server IP addresses
    private final Scanner scanner;
    private List<BruteForceService> servers;
    private ExecutorService executor;
    
    public BruteForceClient() {
        this.scanner = new Scanner(System.in);
        this.servers = new ArrayList<>();
        this.serverHosts = new String[2];  // Support up to 2 servers
    }
    
    /**
     * Run the client application
     */
    public void run() {
        printBanner();
        
        boolean keepRunning = true;
        
        while (keepRunning) {
            try {
                // Get user input
                String targetHash = getTargetHash();
                int passwordLength = getPasswordLength();
                int numServers = getNumberOfServers();
                
                // Get IP address for EACH server
                getServerHosts(numServers);
                
                int threadsPerServer = getThreadsPerServer();
                
                // Display configuration
                displayConfiguration(targetHash, passwordLength, numServers, threadsPerServer);
                
                // Connect to servers with retry option
                if (!connectToServersWithRetry(numServers)) {
                    // User chose to exit
                    keepRunning = askToContinue();
                    continue;
                }
                
                // Start the distributed search
                SearchResult result = executeDistributedSearch(targetHash, passwordLength, 
                        numServers, threadsPerServer);
                
                // Display results
                displayResults(result);
                
                // Ask if user wants to crack another hash
                keepRunning = askToContinue();
                
            } catch (Exception e) {
                System.err.println("\n[ERROR] " + e.getMessage());
                e.printStackTrace();
                keepRunning = askToContinue();
            } finally {
                // Clear servers list for next run
                servers.clear();
            }
        }
        
        System.out.println("\nThank you for using MD5 Brute-Force Cracker!");
        cleanup();
    }
    
    /**
     * Ask user if they want to continue
     */
    private boolean askToContinue() {
        System.out.println();
        System.out.print("Do you want to try again? (y/n): ");
        String answer = scanner.nextLine().trim().toLowerCase();
        System.out.println();
        return answer.equals("y") || answer.equals("yes");
    }
    
    /**
     * Connect to servers with retry option
     */
    private boolean connectToServersWithRetry(int numServers) {
        while (true) {
            if (connectToServers(numServers)) {
                return true;
            }
            
            // Connection failed - show options
            System.out.println();
            System.out.println("================================================================");
            System.out.println("  CONNECTION FAILED!");
            System.out.println("================================================================");
            System.out.println("  Make sure:");
            System.out.println("  1. Server(s) are running (start-server-1, etc.)");
            System.out.println("  2. Firewall is open on server machines");
            System.out.println("  3. IP addresses are correct");
            System.out.println("================================================================");
            System.out.println();
            System.out.println("Options:");
            System.out.println("  [R] Retry connection");
            System.out.println("  [C] Change server IP addresses");
            System.out.println("  [Q] Quit to main menu");
            System.out.println();
            System.out.print("Enter choice (R/C/Q): ");
            
            String choice = scanner.nextLine().trim().toLowerCase();
            
            switch (choice) {
                case "r":
                case "retry":
                    System.out.println("\nRetrying connection...\n");
                    // Connection will be retried in next loop iteration
                    break;
                case "c":
                case "change":
                    getServerHosts(numServers);
                    System.out.println();
                    break;
                case "q":
                case "quit":
                    return false;
                default:
                    System.out.println("Invalid choice. Please enter R, C, or Q.\n");
            }
        }
    }
    
    /**
     * Print the application banner
     */
    private void printBanner() {
        System.out.println("+--------------------------------------------------------------+");
        System.out.println("|     DISTRIBUTED MD5 BRUTE-FORCE PASSWORD CRACKER             |");
        System.out.println("|                    TMN4013 Assignment 2                      |");
        System.out.println("+--------------------------------------------------------------+");
        System.out.println("|  Character Set: ASCII 33-126 (94 printable characters)       |");
        System.out.println("|  Includes: A-Z, a-z, 0-9, and special characters             |");
        System.out.println("|  Supports: 1-2 servers, multiple threads per server          |");
        System.out.println("+--------------------------------------------------------------+");
        System.out.println();
    }
    
    /**
     * Get server host addresses from user (one IP per server)
     */
    private void getServerHosts(int numServers) {
        System.out.println();
        System.out.println("Enter IP address for each server:");
        System.out.println("  (Press Enter for localhost, or type the server's IP)");
        System.out.println();
        
        for (int i = 0; i < numServers; i++) {
            System.out.print("  Server " + (i + 1) + " IP address: ");
            String host = scanner.nextLine().trim();
            if (host.isEmpty()) {
                host = "localhost";
            }
            serverHosts[i] = host;
            System.out.println("    -> Server " + (i + 1) + ": " + host);
        }
    }
    
    /**
     * Get the target MD5 hash from user
     */
    private String getTargetHash() {
        while (true) {
            System.out.print("Enter MD5 hash to crack: ");
            String hash = scanner.nextLine().trim();
            
            if (MD5Util.isValidMD5(hash)) {
                return hash.toLowerCase();
            }
            
            System.err.println("Invalid MD5 hash. Must be 32 hexadecimal characters.");
        }
    }
    
    /**
     * Get password length from user
     */
    private int getPasswordLength() {
        while (true) {
            System.out.print("Enter password length (1-6): ");
            try {
                int length = Integer.parseInt(scanner.nextLine().trim());
                if (length >= 1 && length <= 6) {
                    long searchSpace = MD5Util.calculateSearchSpace(length);
                    System.out.println("  -> Search space: " + String.format("%,d", searchSpace) + " passwords");
                    return length;
                }
                System.err.println("Password length must be between 1 and 6.");
            } catch (NumberFormatException e) {
                System.err.println("Please enter a valid number.");
            }
        }
    }
    
    /**
     * Get number of servers from user
     */
    private int getNumberOfServers() {
        while (true) {
            System.out.print("Enter number of servers (1 or 2): ");
            try {
                int servers = Integer.parseInt(scanner.nextLine().trim());
                if (servers == 1 || servers == 2) {
                    return servers;
                }
                System.err.println("Number of servers must be 1 or 2.");
            } catch (NumberFormatException e) {
                System.err.println("Please enter a valid number.");
            }
        }
    }
    
    /**
     * Get threads per server from user
     */
    private int getThreadsPerServer() {
        while (true) {
            System.out.print("Enter threads per server (1-10): ");
            try {
                int threads = Integer.parseInt(scanner.nextLine().trim());
                if (threads >= 1 && threads <= 10) {
                    return threads;
                }
                System.err.println("Threads per server must be between 1 and 10.");
            } catch (NumberFormatException e) {
                System.err.println("Please enter a valid number.");
            }
        }
    }
    
    /**
     * Display the search configuration
     */
    private void displayConfiguration(String hash, int length, int numServers, int threads) {
        System.out.println();
        System.out.println("================================================================");
        System.out.println("SEARCH CONFIGURATION:");
        System.out.println("  Target Hash:        " + hash);
        System.out.println("  Password Length:    " + length);
        System.out.println("  Number of Servers:  " + numServers);
        for (int i = 0; i < numServers; i++) {
            System.out.println("    Server " + (i + 1) + " IP:     " + serverHosts[i]);
        }
        System.out.println("  Threads per Server: " + threads);
        System.out.println("  Total Threads:      " + (numServers * threads));
        System.out.println("================================================================");
        System.out.println();
    }
    
    /**
     * Connect to RMI servers (each server can be on a different IP)
     */
    private boolean connectToServers(int numServers) {
        System.out.println("Connecting to RMI servers...");
        
        // Clear any existing connections
        servers.clear();
        
        for (int i = 1; i <= numServers; i++) {
            String serverHost = serverHosts[i - 1];  // Get IP for this server
            String serviceName = RMIServer.SERVICE_NAME_PREFIX + i;
            System.out.print("  Connecting to " + serviceName + " at " + serverHost + "... ");
            
            try {
                // Get registry from this server's IP address
                Registry registry = LocateRegistry.getRegistry(serverHost, RMIServer.RMI_PORT);
                BruteForceService service = (BruteForceService) registry.lookup(serviceName);
                
                // Test connection with actual call
                if (service.isAlive()) {
                    servers.add(service);
                    System.out.println("OK (" + service.getServerName() + ")");
                } else {
                    System.out.println("FAILED (server not responding)");
                    servers.clear();
                    return false;
                }
            } catch (java.rmi.ConnectException e) {
                System.out.println("FAILED");
                System.out.println("    [!] Server " + i + " is DOWN or unreachable at " + serverHost);
                System.out.println("    [!] Make sure start-server-" + i + " is running on " + serverHost);
                servers.clear();
                return false;
            } catch (java.rmi.NotBoundException e) {
                System.out.println("FAILED");
                System.out.println("    [!] Server " + i + " is not registered at " + serverHost);
                System.out.println("    [!] The server might still be starting up - wait a few seconds");
                servers.clear();
                return false;
            } catch (java.rmi.RemoteException e) {
                System.out.println("FAILED");
                System.out.println("    [!] RMI connection error: " + e.getMessage());
                System.out.println("    [!] Check if server is running and firewall is open");
                servers.clear();
                return false;
            } catch (Exception e) {
                System.out.println("FAILED");
                System.out.println("    [!] Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                servers.clear();
                return false;
            }
        }
        
        System.out.println("  All " + numServers + " server(s) connected successfully!");
        return true;
    }
    
    /**
     * Execute the distributed search across all servers
     */
    private SearchResult executeDistributedSearch(String targetHash, int passwordLength,
                                                   int numServers, int threadsPerServer) {
        System.out.println();
        System.out.println("Starting distributed search...");
        System.out.println("================================================================");
        
        long startTime = System.currentTimeMillis();
        executor = Executors.newFixedThreadPool(numServers);
        
        // Use CompletionService to process results as they complete (first-finished, first-processed)
        CompletionService<SearchResult> completionService = new ExecutorCompletionService<>(executor);
        
        // Submit search tasks to each server
        for (int i = 0; i < servers.size(); i++) {
            final int serverIndex = i;
            final BruteForceService server = servers.get(i);
            
            SearchConfig config = new SearchConfig(
                    targetHash,
                    passwordLength,
                    numServers,
                    serverIndex,
                    threadsPerServer
            );
            
            completionService.submit(() -> {
                try {
                    return server.startSearch(config);
                } catch (Exception e) {
                    System.err.println("Server " + (serverIndex + 1) + " error: " + e.getMessage());
                    return null;
                }
            });
        }
        
        // Wait for results - process whichever server finishes FIRST
        SearchResult foundResult = null;
        int completedCount = 0;
        
        try {
            // Process results as they complete (not in submission order!)
            while (completedCount < servers.size()) {
                try {
                    // take() blocks until ANY server completes
                    Future<SearchResult> future = completionService.take();
                    completedCount++;
                    
                    SearchResult result = future.get();
                    if (result != null && result.isFound()) {
                        foundResult = result;
                        System.out.println("\n*** PASSWORD FOUND! Stopping all servers... ***");
                        // Stop other servers IMMEDIATELY
                        stopAllServers();
                        break;
                    }
                } catch (Exception e) {
                    System.err.println("Error getting result: " + e.getMessage());
                    completedCount++;
                }
            }
            
        } catch (Exception e) {
            System.err.println("Search error: " + e.getMessage());
        } finally {
            executor.shutdownNow(); // Force shutdown any remaining tasks
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        
        // If no result found, create a "not found" result
        if (foundResult == null) {
            foundResult = new SearchResult(0, totalTime, "Client");
        }
        
        return foundResult;
    }
    
    /**
     * Stop all servers
     */
    private void stopAllServers() {
        for (BruteForceService server : servers) {
            try {
                server.stopSearch();
            } catch (Exception e) {
                // Ignore errors during stop
            }
        }
    }
    
    /**
     * Display search results
     */
    private void displayResults(SearchResult result) {
        System.out.println();
        System.out.println("+--------------------------------------------------------------+");
        System.out.println("|                        SEARCH RESULTS                        |");
        System.out.println("+--------------------------------------------------------------+");
        
        if (result.isFound()) {
            System.out.println("|  Status:       PASSWORD FOUND!                               |");
            System.out.printf("|  Password:     %-46s |%n", result.getPassword());
            System.out.printf("|  Found by:     %s, Thread-%d%-27s |%n", 
                    result.getServerName(), result.getThreadId(), "");
            System.out.printf("|  Time taken:   %,d ms%-38s |%n", result.getSearchTimeMs(), "");
        } else {
            System.out.println("|  Status:       PASSWORD NOT FOUND                            |");
            System.out.printf("|  Time taken:   %,d ms%-38s |%n", result.getSearchTimeMs(), "");
            System.out.println("|  Note: Password may not be in the search space              |");
        }
        
        System.out.println("+--------------------------------------------------------------+");
    }
    
    /**
     * Cleanup resources
     */
    private void cleanup() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
        // Don't close scanner here - it's closed when program exits
    }
    
    /**
     * Main entry point
     */
    public static void main(String[] args) {
        BruteForceClient client = new BruteForceClient();
        client.run();
        client.scanner.close();
    }
}
