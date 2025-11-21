package MD5_BruteForce;

import java.util.concurrent.atomic.AtomicBoolean;

public class SearchCoordinator {
    private static final AtomicBoolean found = new AtomicBoolean(false);
    private static volatile long startTimeNano = 0L;
    private static volatile String foundPassword = null;
    private static volatile int foundThreadId = -1;
    private static volatile int foundServer = -1;

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

    public static void reportFound(String password, int threadId, int server) {
        
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
