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

/**
 * BruteForceClient - Command-line client for distributed MD5 brute-force
 * Connects to RMI servers and coordinates the distributed search
 */
public class BruteForceClient {
    
    private static final String RMI_HOST = "localhost";
    private final Scanner scanner;
    private List<BruteForceService> servers;
    private ExecutorService executor;
    
    public BruteForceClient() {
        this.scanner = new Scanner(System.in);
        this.servers = new ArrayList<>();
    }
    
    /**
     * Run the client application
     */
    public void run() {
        printBanner();
        
        try {
            // Get user input
            String targetHash = getTargetHash();
            int passwordLength = getPasswordLength();
            int numServers = getNumberOfServers();
            int threadsPerServer = getThreadsPerServer();
            
            // Display configuration
            displayConfiguration(targetHash, passwordLength, numServers, threadsPerServer);
            
            // Connect to servers
            if (!connectToServers(numServers)) {
                System.err.println("Failed to connect to required servers. Exiting.");
                return;
            }
            
            // Start the distributed search
            SearchResult result = executeDistributedSearch(targetHash, passwordLength, 
                    numServers, threadsPerServer);
            
            // Display results
            displayResults(result);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }
    
    /**
     * Print the application banner
     */
    private void printBanner() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║     DISTRIBUTED MD5 BRUTE-FORCE PASSWORD CRACKER             ║");
        System.out.println("║                    TMN4013 Assignment 2                      ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  Character Set: a-z, 0-9 (36 characters)                     ║");
        System.out.println("║  Supports: 1-2 servers, multiple threads per server          ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
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
            System.out.print("Enter threads per server (1-16): ");
            try {
                int threads = Integer.parseInt(scanner.nextLine().trim());
                if (threads >= 1 && threads <= 16) {
                    return threads;
                }
                System.err.println("Threads per server must be between 1 and 16.");
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
        System.out.println("════════════════════════════════════════════════════════════════");
        System.out.println("SEARCH CONFIGURATION:");
        System.out.println("  Target Hash:        " + hash);
        System.out.println("  Password Length:    " + length);
        System.out.println("  Number of Servers:  " + numServers);
        System.out.println("  Threads per Server: " + threads);
        System.out.println("  Total Threads:      " + (numServers * threads));
        System.out.println("════════════════════════════════════════════════════════════════");
        System.out.println();
    }
    
    /**
     * Connect to RMI servers
     */
    private boolean connectToServers(int numServers) {
        System.out.println("Connecting to RMI servers...");
        
        try {
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMIServer.RMI_PORT);
            
            for (int i = 1; i <= numServers; i++) {
                String serviceName = RMIServer.SERVICE_NAME_PREFIX + i;
                System.out.print("  Connecting to " + serviceName + "... ");
                
                try {
                    BruteForceService service = (BruteForceService) registry.lookup(serviceName);
                    
                    // Test connection
                    if (service.isAlive()) {
                        servers.add(service);
                        System.out.println("OK (" + service.getServerName() + ")");
                    } else {
                        System.out.println("FAILED (not responding)");
                        return false;
                    }
                } catch (Exception e) {
                    System.out.println("FAILED (" + e.getMessage() + ")");
                    return false;
                }
            }
            
            System.out.println("  All servers connected successfully!");
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to connect to RMI registry: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Execute the distributed search across all servers
     */
    private SearchResult executeDistributedSearch(String targetHash, int passwordLength,
                                                   int numServers, int threadsPerServer) {
        System.out.println();
        System.out.println("Starting distributed search...");
        System.out.println("════════════════════════════════════════════════════════════════");
        
        long startTime = System.currentTimeMillis();
        executor = Executors.newFixedThreadPool(numServers);
        
        List<Future<SearchResult>> futures = new ArrayList<>();
        
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
            
            Future<SearchResult> future = executor.submit(() -> {
                try {
                    return server.startSearch(config);
                } catch (Exception e) {
                    System.err.println("Server " + (serverIndex + 1) + " error: " + e.getMessage());
                    return null;
                }
            });
            
            futures.add(future);
        }
        
        // Wait for results
        SearchResult foundResult = null;
        
        try {
            // Shutdown executor to stop accepting new tasks
            executor.shutdown();
            
            // Wait for all results or until password is found
            for (Future<SearchResult> future : futures) {
                try {
                    SearchResult result = future.get();
                    if (result != null && result.isFound()) {
                        foundResult = result;
                        // Stop other servers
                        stopAllServers();
                        break;
                    }
                } catch (Exception e) {
                    System.err.println("Error getting result: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Search error: " + e.getMessage());
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
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                        SEARCH RESULTS                        ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        
        if (result.isFound()) {
            System.out.println("║  Status:       PASSWORD FOUND!                               ║");
            System.out.printf("║  Password:     %-46s ║%n", result.getPassword());
            System.out.printf("║  Found by:     %s, Thread-%d%-27s ║%n", 
                    result.getServerName(), result.getThreadId(), "");
            System.out.printf("║  Time taken:   %,d ms%-38s ║%n", result.getSearchTimeMs(), "");
        } else {
            System.out.println("║  Status:       PASSWORD NOT FOUND                            ║");
            System.out.printf("║  Time taken:   %,d ms%-38s ║%n", result.getSearchTimeMs(), "");
            System.out.println("║  Note: Password may not be in the search space              ║");
        }
        
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }
    
    /**
     * Cleanup resources
     */
    private void cleanup() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
        scanner.close();
    }
    
    /**
     * Main entry point
     */
    public static void main(String[] args) {
        BruteForceClient client = new BruteForceClient();
        client.run();
    }
}
