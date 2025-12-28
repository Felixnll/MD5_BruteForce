package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ServerLogger - Logging utility for server operations
 * Creates separate log files for each server instance
 */
public class ServerLogger {
    
    private final String serverName;
    private final String logFileName;
    private PrintWriter logWriter;
    private final SimpleDateFormat dateFormat;
    
    /**
     * Create a new logger for a server
     * 
     * @param serverIndex Server index (1-based for file naming)
     */
    public ServerLogger(int serverIndex) {
        this.serverName = "Server_" + serverIndex;
        this.logFileName = "server_" + serverIndex + ".log";
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        initializeLogFile();
    }
    
    /**
     * Initialize the log file
     */
    private void initializeLogFile() {
        try {
            logWriter = new PrintWriter(new FileWriter(logFileName, false));
            log("========================================");
            log("Server Log Initialized: " + serverName);
            log("Log File: " + logFileName);
            log("========================================");
        } catch (IOException e) {
            System.err.println("Failed to create log file: " + e.getMessage());
        }
    }
    
    /**
     * Log a message with timestamp
     * 
     * @param message Message to log
     */
    public synchronized void log(String message) {
        String timestamp = dateFormat.format(new Date());
        String logEntry = String.format("[%s] %s", timestamp, message);
        
        // Write to file
        if (logWriter != null) {
            logWriter.println(logEntry);
            logWriter.flush();
        }
        
        // Also print to console
        System.out.println("[" + serverName + "] " + logEntry);
    }
    
    /**
     * Log server start event
     */
    public void logServerStart() {
        log("SERVER STARTED");
        log("RMI Service registered and ready");
    }
    
    /**
     * Log search start event
     * 
     * @param numThreads Number of threads created
     * @param targetHash Target MD5 hash
     * @param passwordLength Password length to search
     */
    public void logSearchStart(int numThreads, String targetHash, int passwordLength) {
        log("----------------------------------------");
        log("SEARCH STARTED");
        log("Target Hash: " + targetHash);
        log("Password Length: " + passwordLength);
        log("Threads Created: " + numThreads);
    }
    
    /**
     * Log thread range assignment
     * 
     * @param threadId Thread identifier
     * @param startIndex Start of search range
     * @param endIndex End of search range
     */
    public void logThreadRange(int threadId, long startIndex, long endIndex) {
        log(String.format("Thread-%d assigned range: [%d - %d] (size: %d)",
                threadId, startIndex, endIndex, endIndex - startIndex));
    }
    
    /**
     * Log thread start event
     * 
     * @param threadId Thread identifier
     */
    public void logThreadStart(int threadId) {
        log(String.format("Thread-%d STARTED searching", threadId));
    }
    
    /**
     * Log thread stop event
     * 
     * @param threadId Thread identifier
     * @param reason Reason for stopping
     */
    public void logThreadStop(int threadId, String reason) {
        log(String.format("Thread-%d STOPPED - %s", threadId, reason));
    }
    
    /**
     * Log password found event
     * 
     * @param threadId Thread that found the password
     * @param password The found password
     * @param timeMs Time taken
     */
    public void logPasswordFound(int threadId, String password, long timeMs) {
        log("========================================");
        log("PASSWORD FOUND!");
        log("Found by: Thread-" + threadId);
        log("Password: " + password);
        log("Time taken: " + timeMs + " ms");
        log("========================================");
    }
    
    /**
     * Log search completion (not found)
     * 
     * @param timeMs Time taken
     */
    public void logSearchComplete(long timeMs) {
        log("Search completed in " + timeMs + " ms");
        log("Password NOT found in assigned range");
    }
    
    /**
     * Log an exception
     * 
     * @param context Context where exception occurred
     * @param e The exception
     */
    public void logException(String context, Exception e) {
        log("EXCEPTION in " + context + ": " + e.getMessage());
        if (logWriter != null) {
            e.printStackTrace(logWriter);
            logWriter.flush();
        }
    }
    
    /**
     * Log server shutdown
     */
    public void logServerShutdown() {
        log("----------------------------------------");
        log("SERVER SHUTDOWN");
        log("========================================");
    }
    
    /**
     * Close the log file
     */
    public void close() {
        if (logWriter != null) {
            logWriter.close();
        }
    }
}
