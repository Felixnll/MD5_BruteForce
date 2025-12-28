package common;

import java.io.Serializable;

/**
 * SearchResult - Result object returned from server to client
 * Contains the found password and metadata about the search
 */
public class SearchResult implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final boolean found;           // Whether password was found
    private final String password;         // The found password (null if not found)
    private final int serverIndex;         // Which server found it
    private final int threadId;            // Which thread found it
    private final long searchTimeMs;       // Time taken in milliseconds
    private final String serverName;       // Name of the server
    
    /**
     * Constructor for successful search result
     */
    public SearchResult(String password, int serverIndex, int threadId, 
                        long searchTimeMs, String serverName) {
        this.found = true;
        this.password = password;
        this.serverIndex = serverIndex;
        this.threadId = threadId;
        this.searchTimeMs = searchTimeMs;
        this.serverName = serverName;
    }
    
    /**
     * Constructor for unsuccessful search result
     */
    public SearchResult(int serverIndex, long searchTimeMs, String serverName) {
        this.found = false;
        this.password = null;
        this.serverIndex = serverIndex;
        this.threadId = -1;
        this.searchTimeMs = searchTimeMs;
        this.serverName = serverName;
    }
    
    public boolean isFound() {
        return found;
    }
    
    public String getPassword() {
        return password;
    }
    
    public int getServerIndex() {
        return serverIndex;
    }
    
    public int getThreadId() {
        return threadId;
    }
    
    public long getSearchTimeMs() {
        return searchTimeMs;
    }
    
    public String getServerName() {
        return serverName;
    }
    
    @Override
    public String toString() {
        if (found) {
            return String.format("SearchResult[FOUND: '%s' by %s Thread-%d in %dms]",
                    password, serverName, threadId, searchTimeMs);
        } else {
            return String.format("SearchResult[NOT FOUND by %s in %dms]",
                    serverName, searchTimeMs);
        }
    }
}
