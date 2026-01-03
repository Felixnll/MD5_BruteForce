package MD5_BruteForce;

/**
 * GlobalDistributor - Contains global constants for the search space
 * 
 * NOTE: With STATIC PARTITIONING, we no longer use shared atomic counters.
 * Each thread now has its own deterministic, non-overlapping search range.
 * 
 * STATIC PARTITIONING SCHEME:
 * - Total ASCII range: [33, 127) = 94 printable characters
 * - Server 1: first-char range [33, 80) = 47 chars ('!' to 'O')
 * - Server 2: first-char range [80, 127) = 47 chars ('P' to '~')
 * - Each thread within a server gets a sub-range of first characters
 * - Each thread searches ALL suffix combinations for its assigned first-chars
 * - NO overlap, NO dynamic work stealing - fully deterministic
 */
public class GlobalDistributor {

    // ASCII range constants
    public static final int START = 33; // '!' in ASCII
    public static final int END = 127; // DEL (exclusive)
    public static final int BASE = 94; // total characters in range (END - START)
    public static final int BASE_OFFSET = 33; // offset for base-94 conversion

    // Server range boundaries (for documentation/reference)
    public static final int SERVER1_START = 33;  // '!'
    public static final int SERVER1_END = 80;    // 'P' (exclusive) - 47 chars
    public static final int SERVER2_START = 80;  // 'P'
    public static final int SERVER2_END = 127;   // DEL (exclusive) - 47 chars

    // Helper method to calculate total passwords for a given length
    public static long getTotalPasswords(int length) {
        return pow(BASE, length);
    }

    // Helper method to calculate passwords for a first-char range at given length
    public static long getPasswordsForRange(int firstCharStart, int firstCharEnd, int length) {
        int firstCharCount = firstCharEnd - firstCharStart;
        if (length == 1) {
            return firstCharCount;
        }
        return firstCharCount * pow(BASE, length - 1);
    }

    // Simple power function (base^exp)
    private static long pow(int base, int exp) {
        long r = 1L;
        for (int i = 0; i < exp; i++) r *= base;
        return r;
    }
}
