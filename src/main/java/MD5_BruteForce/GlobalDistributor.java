package MD5_BruteForce;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages work distribution across all threads
 * Uses atomic counters to ensure threads don't duplicate work
 */
public class GlobalDistributor {

    public static final int START = 33; // '!' in ASCII
    public static final int END = 127; // DEL (exclusive)
    public static final int BASE = 94; // total characters in range
    public static final int BASE_OFFSET = 33; // offset for base conversion
    public static final int CHUNK_SIZE = 1024; // how many passwords per chunk

    // Counters for each password length (1-6)
    public static AtomicLong[] counters = new AtomicLong[7];
    // Total number of passwords for each length
    public static long[] totals = new long[7];

    // Initialize counters and totals
    public static void init() {
        int range = END - START; // how many possible first characters
        // Calculate totals for each password length
        for (int len = 1; len <= 6; len++) {
            if (len == 1) {
                totals[len] = range; // just the first character
            } else {
                // For length > 1: range * (BASE ^ (len-1))
                long suffix = pow(BASE, len - 1);
                totals[len] = range * suffix;
            }
            counters[len] = new AtomicLong(0L); // start at 0
        }
    }

    // Reset all counters back to 0 (for new search)
    public static void reset() {
        for (int i = 1; i <= 6; i++) {
            if (counters[i] != null) counters[i].set(0L); // reset to 0
        }
    }

    // Simple power function (base^exp)
    // Could use Math.pow but this avoids floating point
    private static long pow(int base, int exp) {
        long r = 1L;
        for (int i = 0; i < exp; i++) r *= base; // multiply base, exp times
        return r;
    }
}
