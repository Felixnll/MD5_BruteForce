package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5Util - Utility class for MD5 hash operations
 * Provides methods for generating and comparing MD5 hashes
 */
public class MD5Util {
    
    // Thread-local MessageDigest for thread safety
    private static final ThreadLocal<MessageDigest> MD5_DIGEST = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    });
    
    // Character set for password generation (ASCII 33-126: all printable characters)
    // Includes: !"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`abcdefghijklmnopqrstuvwxyz{|}~
    public static final String CHARSET;
    public static final int CHARSET_SIZE;
    
    static {
        // Build character set from ASCII 33 to 126 (94 printable characters)
        StringBuilder sb = new StringBuilder();
        for (int i = 33; i <= 126; i++) {
            sb.append((char) i);
        }
        CHARSET = sb.toString();
        CHARSET_SIZE = CHARSET.length(); // 94 characters
    }
    
    /**
     * Generate MD5 hash of a string
     * 
     * @param input The string to hash
     * @return MD5 hash as lowercase hex string
     */
    public static String md5(String input) {
        MessageDigest md = MD5_DIGEST.get();
        md.reset();
        byte[] digest = md.digest(input.getBytes());
        return bytesToHex(digest);
    }
    
    /**
     * Convert byte array to hex string
     * 
     * @param bytes Byte array to convert
     * @return Lowercase hex string
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(32);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
    
    /**
     * Convert a numeric index to a password string
     * Maps index 0 -> "aaa...", index 1 -> "aab...", etc.
     * 
     * @param index The numeric index
     * @param length The password length
     * @return The password string
     */
    public static String indexToPassword(long index, int length) {
        char[] password = new char[length];
        for (int i = length - 1; i >= 0; i--) {
            password[i] = CHARSET.charAt((int) (index % CHARSET_SIZE));
            index /= CHARSET_SIZE;
        }
        return new String(password);
    }
    
    /**
     * Calculate the total search space size for a given password length
     * 
     * @param length Password length
     * @return Total number of possible passwords
     */
    public static long calculateSearchSpace(int length) {
        return (long) Math.pow(CHARSET_SIZE, length);
    }
    
    /**
     * Validate if a string is a valid MD5 hash
     * 
     * @param hash String to validate
     * @return true if valid MD5 hash format
     */
    public static boolean isValidMD5(String hash) {
        if (hash == null || hash.length() != 32) {
            return false;
        }
        return hash.matches("[a-fA-F0-9]{32}");
    }
}
