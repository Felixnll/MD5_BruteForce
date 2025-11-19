package MD5_BruteForce;

import java.util.concurrent.atomic.AtomicLong;

public class GlobalDistributor {

    public static final int START = 33;
    public static final int END = 127; // exclusive
    public static final int BASE = 94;
    public static final int BASE_OFFSET = 33;
    public static final int CHUNK_SIZE = 1024;

    public static AtomicLong[] counters = new AtomicLong[7];
    public static long[] totals = new long[7];

    public static void init() {
        int range = END - START;
        for (int len = 1; len <= 6; len++) {
            if (len == 1) {
                totals[len] = range;
            } else {
                long suffix = pow(BASE, len - 1);
                totals[len] = range * suffix;
            }
            counters[len] = new AtomicLong(0L);
        }
    }

    public static void reset() {
        for (int i = 1; i <= 6; i++) {
            if (counters[i] != null) counters[i].set(0L);
        }
    }

    private static long pow(int base, int exp) {
        long r = 1L;
        for (int i = 0; i < exp; i++) r *= base;
        return r;
    }
}
