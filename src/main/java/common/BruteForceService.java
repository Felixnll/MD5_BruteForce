package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * BruteForceService - RMI Remote Interface
 * Defines the contract between client and server for distributed MD5 brute-force
 */
public interface BruteForceService extends Remote {
    
    /**
     * Start a brute-force search with the given configuration
     * This method blocks until the search completes or is stopped
     * 
     * @param config Search configuration
     * @return SearchResult containing the found password or null
     * @throws RemoteException if RMI communication fails
     */
    SearchResult startSearch(SearchConfig config) throws RemoteException;
    
    /**
     * Stop all ongoing searches on this server
     * Called when password is found by another server
     * 
     * @throws RemoteException if RMI communication fails
     */
    void stopSearch() throws RemoteException;
    
    /**
     * Check if the server is alive and ready
     * 
     * @return true if server is ready
     * @throws RemoteException if RMI communication fails
     */
    boolean isAlive() throws RemoteException;
    
    /**
     * Get the server name/identifier
     * 
     * @return Server name
     * @throws RemoteException if RMI communication fails
     */
    String getServerName() throws RemoteException;
}
