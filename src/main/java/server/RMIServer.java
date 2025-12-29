package server;

import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * RMIServer - Main entry point for starting RMI servers
 * Can start one or multiple servers on different ports
 * Supports both local and remote connections
 */
public class RMIServer {
    
    public static final int RMI_PORT = 1099;
    public static final String SERVICE_NAME_PREFIX = "BruteForceService_";
    
    private BruteForceServiceImpl service;
    private int serverIndex;
    private Registry registry;
    
    /**
     * Create and start an RMI server
     * 
     * @param serverIndex Server index (1-based)
     */
    public RMIServer(int serverIndex) {
        this.serverIndex = serverIndex;
    }
    
    /**
     * Start the RMI server and register the service
     */
    public void start() {
        try {
            System.out.println("Starting RMI Server " + serverIndex + "...");
            
            // Get the local IP address for remote connections
            String localIP = InetAddress.getLocalHost().getHostAddress();
            
            // Set the RMI server hostname so clients can connect remotely
            System.setProperty("java.rmi.server.hostname", localIP);
            
            // Create the service implementation
            service = new BruteForceServiceImpl(serverIndex);
            
            // Get or create the RMI registry
            try {
                registry = LocateRegistry.getRegistry(RMI_PORT);
                registry.list(); // Test if registry exists
            } catch (Exception e) {
                System.out.println("Creating new RMI registry on port " + RMI_PORT);
                registry = LocateRegistry.createRegistry(RMI_PORT);
            }
            
            // Register the service
            String serviceName = SERVICE_NAME_PREFIX + serverIndex;
            registry.rebind(serviceName, service);
            
            System.out.println("════════════════════════════════════════════════════════════════");
            System.out.println("  RMI Server " + serverIndex + " is ready!");
            System.out.println("════════════════════════════════════════════════════════════════");
            System.out.println("  Service Name: " + serviceName);
            System.out.println("  RMI Port:     " + RMI_PORT);
            System.out.println("  Local IP:     " + localIP);
            System.out.println("════════════════════════════════════════════════════════════════");
            System.out.println("  FOR REMOTE CLIENTS:");
            System.out.println("  Enter this IP: " + localIP);
            System.out.println("════════════════════════════════════════════════════════════════");
            System.out.println("Press Ctrl+C to shutdown...");
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down server...");
                shutdown();
            }));
            
            // Keep server running
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Shutdown the server
     */
    public void shutdown() {
        try {
            if (service != null) {
                service.shutdown();
                String serviceName = SERVICE_NAME_PREFIX + serverIndex;
                try {
                    registry.unbind(serviceName);
                    UnicastRemoteObject.unexportObject(service, true);
                } catch (Exception e) {
                    // Ignore unbind errors during shutdown
                }
            }
        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
    }
    
    /**
     * Main entry point
     * Usage: java server.RMIServer [serverIndex]
     */
    public static void main(String[] args) {
        int serverIndex = 1;
        
        if (args.length > 0) {
            try {
                serverIndex = Integer.parseInt(args[0]);
                if (serverIndex < 1 || serverIndex > 10) {
                    System.err.println("Server index must be between 1 and 10");
                    System.exit(1);
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid server index: " + args[0]);
                System.exit(1);
            }
        }
        
        RMIServer server = new RMIServer(serverIndex);
        server.start();
    }
}
