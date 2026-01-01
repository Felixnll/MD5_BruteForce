package server;

import common.SearchRange;
import util.ServerLogger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SearchThread - Worker thread for brute-force MD5 password searching
 * Each thread searches a specific range of the password space
 * 
 * OPTIMIZED: Uses direct byte manipulation like Assignment 1 for maximum speed
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
    
    // OPTIMIZATION: Pre-allocated buffers for zero-allocation in hot loop
    private MessageDigest md;
    private byte[] inputBytes = new byte[6]; // max password length
    private byte[] targetDigest; // target hash as bytes
    private byte[] digestBuffer = new byte[16]; // MD5 output buffer
    
    // Character set constants (ASCII 33-126)
    private static final int CHARSET_START = 33;
    private static final int CHARSET_SIZE = 94;
    
    /**
     * Create a new search thread
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
        
        // Initialize MD5 digest
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not available", e);
        }
        
        // Convert target hash string to byte array for fast comparison
        this.targetDigest = hexStringToByteArray(this.targetHash);
    }
    
    /**
     * Convert hex string to byte array
     */
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int hi = Character.digit(s.charAt(i), 16);
            int lo = Character.digit(s.charAt(i + 1), 16);
            data[i / 2] = (byte) ((hi << 4) + lo);
        }
        return data;
    }
    
    /**
     * FAST: Convert index to password bytes directly (no String allocation)
     */
    private void indexToPasswordBytes(long index, int length) {
        for (int i = length - 1; i >= 0; i--) {
            inputBytes[i] = (byte) (CHARSET_START + (int)(index % CHARSET_SIZE));
            index /= CHARSET_SIZE;
        }
    }
    
    /**
     * FAST: Check if current password matches target hash (byte comparison)
     */
    private boolean md5Matches(int length) {
        md.reset();
        md.update(inputBytes, 0, length);
        try {
            md.digest(digestBuffer, 0, 16);
        } catch (Exception e) {
            return false;
        }
        return MessageDigest.isEqual(digestBuffer, targetDigest);
    }
    
    @Override
    public void run() {
        logger.logThreadStart(threadId);
        long startTime = System.currentTimeMillis();
        long attemptCount = 0;
        long lastLogTime = startTime;
        final long LOG_INTERVAL = 50_000_000; // Log every 50 million attempts
        
        try {
            // OPTIMIZED: Use direct byte manipulation - no String allocation in hot loop
            for (long index = range.getStartIndex(); index < range.getEndIndex() && running; index++) {
                // Check if we should stop (password found elsewhere)
                if (!running || coordinator.isPasswordFound()) {
                    break;
                }
                
                // FAST: Convert index directly to bytes (no String allocation)
                indexToPasswordBytes(index, passwordLength);
                
                attemptCount++;
                
                // FAST: Compare MD5 hashes as byte arrays
                if (md5Matches(passwordLength)) {
                    // Only create String when password is actually found
                    foundPassword = new String(inputBytes, 0, passwordLength);
                    long elapsed = System.currentTimeMillis() - startTime;
                    
                    // Report found password to coordinator
                    coordinator.reportFound(threadId, foundPassword, elapsed);
                    logger.logPasswordFound(threadId, foundPassword, elapsed);
                    return;
                }
                
                // Progress logging (less frequently to reduce overhead)
                if (attemptCount % LOG_INTERVAL == 0) {
                    long now = System.currentTimeMillis();
                    long elapsed = now - lastLogTime;
                    double rate = (elapsed > 0) ? (LOG_INTERVAL * 1000.0 / elapsed) : 0;
                    double progress = (100.0 * attemptCount) / range.getSize();
                    String currentPassword = new String(inputBytes, 0, passwordLength);
                    logger.log(String.format("Thread-%d: %.2f%% done, %,d attempts, %.0f/sec, current: %s",
                            threadId, progress, attemptCount, rate, currentPassword));
                    lastLogTime = now;
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
