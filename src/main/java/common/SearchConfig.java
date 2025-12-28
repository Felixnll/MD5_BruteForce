package common;

import java.io.Serializable;

/**
 * SearchConfig - Configuration object passed from client to servers
 * Contains all parameters needed to perform a distributed MD5 brute-force search
 */
public class SearchConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String targetHash;      // MD5 hash to find
    private final int passwordLength;      // Length of password to search
    private final int totalServers;        // Total number of servers
    private final int serverIndex;         // This server's index (0-based)
    private final int threadsPerServer;    // Number of threads per server
    
    public SearchConfig(String targetHash, int passwordLength, int totalServers, 
                        int serverIndex, int threadsPerServer) {
        this.targetHash = targetHash.toLowerCase();
        this.passwordLength = passwordLength;
        this.totalServers = totalServers;
        this.serverIndex = serverIndex;
        this.threadsPerServer = threadsPerServer;
    }
    
    public String getTargetHash() {
        return targetHash;
    }
    
    public int getPasswordLength() {
        return passwordLength;
    }
    
    public int getTotalServers() {
        return totalServers;
    }
    
    public int getServerIndex() {
        return serverIndex;
    }
    
    public int getThreadsPerServer() {
        return threadsPerServer;
    }
    
    @Override
    public String toString() {
        return String.format("SearchConfig[hash=%s, length=%d, servers=%d, serverIdx=%d, threads=%d]",
                targetHash.substring(0, 8) + "...", passwordLength, totalServers, serverIndex, threadsPerServer);
    }
}
