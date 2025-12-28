package server;

import common.BruteForceService;
import common.SearchConfig;
import common.SearchResult;
import util.ServerLogger;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * BruteForceServiceImpl - RMI Server Implementation
 * Implements the remote interface for distributed MD5 brute-force searching
 */
public class BruteForceServiceImpl extends UnicastRemoteObject implements BruteForceService {
    
    private static final long serialVersionUID = 1L;
    
    private final String serverName;
    private final ServerLogger logger;
    private SearchCoordinator coordinator;
    private volatile boolean isSearching = false;
    
    /**
     * Create a new BruteForceService implementation
     * 
     * @param serverIndex Index of this server (1-based for naming)
     * @throws RemoteException if RMI setup fails
     */
    public BruteForceServiceImpl(int serverIndex) throws RemoteException {
        super();
        this.serverName = "Server_" + serverIndex;
        this.logger = new ServerLogger(serverIndex);
        logger.logServerStart();
    }
    
    @Override
    public SearchResult startSearch(SearchConfig config) throws RemoteException {
        if (isSearching) {
            logger.log("WARNING: Search already in progress, stopping previous search");
            stopSearch();
        }
        
        isSearching = true;
        logger.log("Received search request: " + config);
        
        try {
            // Create coordinator and execute search
            coordinator = new SearchCoordinator(config.getServerIndex(), serverName, logger);
            SearchResult result = coordinator.executeSearch(config);
            
            logger.log("Search completed: " + result);
            return result;
            
        } catch (Exception e) {
            logger.logException("startSearch", e);
            throw new RemoteException("Search failed: " + e.getMessage(), e);
        } finally {
            isSearching = false;
        }
    }
    
    @Override
    public void stopSearch() throws RemoteException {
        logger.log("Stop search requested");
        if (coordinator != null) {
            coordinator.stopAllThreads();
        }
        isSearching = false;
    }
    
    @Override
    public boolean isAlive() throws RemoteException {
        return true;
    }
    
    @Override
    public String getServerName() throws RemoteException {
        return serverName;
    }
    
    /**
     * Get the logger for this server
     */
    public ServerLogger getLogger() {
        return logger;
    }
    
    /**
     * Shutdown this server
     */
    public void shutdown() {
        logger.logServerShutdown();
        logger.close();
    }
}
