package server;

import common.SearchRange;
import util.MD5Util;
import util.ServerLogger;

/**
 * SearchThread - Worker thread for brute-force MD5 password searching
 * Each thread searches a specific range of the password space
 */
public class SearchThread extends Thread {
    
    private final int threadId;
    private final SearchRange range;
    private final String targetHash;
    private final int passwordLength;
    private final SearchCoordinator coordinator;
    private final ServerLogger logger;
    
    private volatile boolean running = true;
    private String foundPassword = null;
    
    /**
     * Create a new search thread
     * 
     * @param threadId Unique thread identifier
     * @param range Search range for this thread
     * @param targetHash MD5 hash to find
     * @param passwordLength Length of passwords to generate
     * @param coordinator Coordinator for reporting results
     * @param logger Logger for this server
     */
    public SearchThread(int threadId, SearchRange range, String targetHash,
                        int passwordLength, SearchCoordinator coordinator, ServerLogger logger) {
        super("SearchThread-" + threadId);
        this.threadId = threadId;
        this.range = range;
        this.targetHash = targetHash.toLowerCase();
        this.passwordLength = passwordLength;
        this.coordinator = coordinator;
        this.logger = logger;
    }
    
    @Override
    public void run() {
        logger.logThreadStart(threadId);
        long startTime = System.currentTimeMillis();
        
        try {
            // Iterate through the assigned range
            for (long index = range.getStartIndex(); index < range.getEndIndex() && running; index++) {
                // Check if we should stop (password found elsewhere)
                if (!running || coordinator.isPasswordFound()) {
                    break;
                }
                
                // Convert index to password string
                String candidate = MD5Util.indexToPassword(index, passwordLength);
                
                // Generate MD5 hash of candidate
                String hash = MD5Util.md5(candidate);
                
                // Check if hash matches target
                if (hash.equals(targetHash)) {
                    foundPassword = candidate;
                    long elapsed = System.currentTimeMillis() - startTime;
                    
                    // Report found password to coordinator
                    coordinator.reportFound(threadId, candidate, elapsed);
                    logger.logPasswordFound(threadId, candidate, elapsed);
                    return;
                }
            }
            
            // Thread finished without finding password
            if (running && !coordinator.isPasswordFound()) {
                logger.logThreadStop(threadId, "Range exhausted, not found");
            } else {
                logger.logThreadStop(threadId, "Stopped by coordinator");
            }
            
        } catch (Exception e) {
            logger.logException("SearchThread-" + threadId, e);
        }
    }
    
    /**
     * Stop this thread's search
     */
    public void stopSearch() {
        running = false;
        this.interrupt();
    }
    
    /**
     * Get the thread ID
     */
    public int getThreadId() {
        return threadId;
    }
    
    /**
     * Get the found password (null if not found)
     */
    public String getFoundPassword() {
        return foundPassword;
    }
    
    /**
     * Check if this thread found the password
     */
    public boolean hasFoundPassword() {
        return foundPassword != null;
    }
}
