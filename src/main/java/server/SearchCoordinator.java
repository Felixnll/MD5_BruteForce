package server;

import common.SearchConfig;
import common.SearchRange;
import common.SearchResult;
import util.MD5Util;
import util.ServerLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * SearchCoordinator - Manages worker threads and coordinates the search
 * Handles range distribution and result aggregation
 */
public class SearchCoordinator {
    
    private final ServerLogger logger;
    private final String serverName;
    private final int serverIndex;
    
    private volatile boolean passwordFound = false;
    private volatile String foundPassword = null;
    private volatile int finderThreadId = -1;
    private volatile long searchStartTime;
    private volatile long searchEndTime;
    
    private List<SearchThread> workerThreads;
    
    /**
     * Create a new search coordinator
     * 
     * @param serverIndex Server index (0-based)
     * @param serverName Name of this server
     * @param logger Logger for this server
     */
    public SearchCoordinator(int serverIndex, String serverName, ServerLogger logger) {
        this.serverIndex = serverIndex;
        this.serverName = serverName;
        this.logger = logger;
        this.workerThreads = new ArrayList<>();
    }
    
    /**
     * Execute a search with the given configuration
     * Creates and manages worker threads
     * 
     * @param config Search configuration
     * @return SearchResult with the outcome
     */
    public SearchResult executeSearch(SearchConfig config) {
        passwordFound = false;
        foundPassword = null;
        finderThreadId = -1;
        workerThreads.clear();
        
        searchStartTime = System.currentTimeMillis();
        int numThreads = config.getThreadsPerServer();
        
        // Calculate this server's range
        SearchRange serverRange = calculateServerRange(config);
        logger.logSearchStart(numThreads, config.getTargetHash(), config.getPasswordLength());
        logger.log("Server range: " + serverRange);
        
        // Check if server has any work to do
        if (serverRange.getSize() <= 0) {
            logger.log("No work assigned to this server");
            return new SearchResult(serverIndex, 0, serverName);
        }
        
        // Calculate thread ranges and create worker threads
        List<SearchRange> threadRanges = divideRange(serverRange, numThreads);
        
        for (int i = 0; i < threadRanges.size(); i++) {
            SearchRange threadRange = threadRanges.get(i);
            logger.logThreadRange(i, threadRange.getStartIndex(), threadRange.getEndIndex());
            
            SearchThread thread = new SearchThread(
                    i,
                    threadRange,
                    config.getTargetHash(),
                    config.getPasswordLength(),
                    this,
                    logger
            );
            workerThreads.add(thread);
        }
        
        // Start all threads
        for (SearchThread thread : workerThreads) {
            thread.start();
        }
        
        // Wait for all threads to complete or password to be found
        for (SearchThread thread : workerThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                logger.logException("Waiting for thread", e);
                Thread.currentThread().interrupt();
            }
        }
        
        searchEndTime = System.currentTimeMillis();
        long elapsed = searchEndTime - searchStartTime;
        
        // Return result
        if (passwordFound) {
            return new SearchResult(foundPassword, serverIndex, finderThreadId, elapsed, serverName);
        } else {
            logger.logSearchComplete(elapsed);
            return new SearchResult(serverIndex, elapsed, serverName);
        }
    }
    
    /**
     * Calculate the search range for this server
     * 
     * @param config Search configuration
     * @return SearchRange for this server
     */
    private SearchRange calculateServerRange(SearchConfig config) {
        long totalSpace = MD5Util.calculateSearchSpace(config.getPasswordLength());
        int totalServers = config.getTotalServers();
        int serverIdx = config.getServerIndex();
        
        // Divide total space by number of servers
        long rangePerServer = totalSpace / totalServers;
        long remainder = totalSpace % totalServers;
        
        // Calculate start and end for this server
        long start = serverIdx * rangePerServer + Math.min(serverIdx, remainder);
        long end = start + rangePerServer + (serverIdx < remainder ? 1 : 0);
        
        return new SearchRange(start, end);
    }
    
    /**
     * Divide a range into sub-ranges for threads
     * 
     * @param range Range to divide
     * @param numThreads Number of threads
     * @return List of thread ranges
     */
    private List<SearchRange> divideRange(SearchRange range, int numThreads) {
        List<SearchRange> ranges = new ArrayList<>();
        long totalSize = range.getSize();
        long rangePerThread = totalSize / numThreads;
        long remainder = totalSize % numThreads;
        
        long currentStart = range.getStartIndex();
        
        for (int i = 0; i < numThreads; i++) {
            long size = rangePerThread + (i < remainder ? 1 : 0);
            if (size > 0) {
                ranges.add(new SearchRange(currentStart, currentStart + size));
                currentStart += size;
            }
        }
        
        return ranges;
    }
    
    /**
     * Called by a worker thread when password is found
     * 
     * @param threadId ID of the thread that found the password
     * @param password The found password
     * @param timeMs Time taken
     */
    public synchronized void reportFound(int threadId, String password, long timeMs) {
        if (!passwordFound) {
            passwordFound = true;
            foundPassword = password;
            finderThreadId = threadId;
            
            // Stop all other threads
            stopAllThreads();
        }
    }
    
    /**
     * Check if password has been found
     */
    public boolean isPasswordFound() {
        return passwordFound;
    }
    
    /**
     * Stop all worker threads
     */
    public void stopAllThreads() {
        logger.log("Stopping all worker threads...");
        for (SearchThread thread : workerThreads) {
            thread.stopSearch();
        }
    }
    
    /**
     * Get the found password
     */
    public String getFoundPassword() {
        return foundPassword;
    }
    
    /**
     * Get the thread ID that found the password
     */
    public int getFinderThreadId() {
        return finderThreadId;
    }
}
