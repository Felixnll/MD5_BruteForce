package MD5_BruteForce;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Coordinates the search across all threads
 * Tracks whether password has been found and by which thread
 */
public class SearchCoordinator {
    // Using AtomicBoolean for thread-safe flag
    private static final AtomicBoolean found = new AtomicBoolean(false);
    private static volatile long startTimeNano = 0L; // when search started
    private static volatile String foundPassword = null; // the password that was found
    private static volatile int foundThreadId = -1; // which thread found it
    private static volatile int foundServer = -1; // which server found it

    public static void reset() {
        found.set(false);
        startTimeNano = 0L;
        foundPassword = null;
        foundThreadId = -1;
        foundServer = -1;
    }

    public static void setStartTime(long t) {
        startTimeNano = t;
    }

    public static long getStartTime() {
        return startTimeNano;
    }

    public static boolean isFound() {
        return found.get();
    }

    public static String getFoundPassword() {
        return foundPassword;
    }

    public static int getFoundThreadId() {
        return foundThreadId;
    }

    public static int getFoundServer() {
        return foundServer;
    }

    // Called by thread when it finds the password
    public static void reportFound(String password, int threadId, int server) {
        // Use compareAndSet to ensure only first thread reports
        // This is atomic - only one thread will get true back
        if (found.compareAndSet(false, true)) {
            foundPassword = password;
            foundThreadId = threadId;
            foundServer = server;
            long end = System.nanoTime();
            double totalSec = (end - startTimeNano) / 1_000_000_000.0;
            System.out.println("Password found : " + password + " by Thread " + threadId + " on Server " + server);
            System.out.printf("Time : %.6f seconds (%.6f minutes)%n", totalSec, totalSec / 60.0);
            
            Server1.stop_threads();
            Server2.stop_threads();
        }
    }
}
