package util;

/**
 * TestHashGenerator - Utility to generate MD5 hashes for testing
 * Use this to create test cases for the brute-force system
 */
public class TestHashGenerator {
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              MD5 HASH GENERATOR FOR TESTING                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // Generate hashes for test passwords
        String[] testPasswords = {
            "a",        // 1 character
            "ab",       // 2 characters
            "abc",      // 3 characters
            "test",     // 4 characters
            "hello",    // 5 characters
            "pass12",   // 6 characters
            "xyz",      // 3 characters
            "cat",      // 3 characters
            "dog",      // 3 characters
            "123",      // 3 characters (digits only)
            "a1b2c3"    // 6 characters (mixed)
        };
        
        System.out.println("Password → MD5 Hash");
        System.out.println("════════════════════════════════════════════════════════════════");
        
        for (String password : testPasswords) {
            String hash = MD5Util.md5(password);
            System.out.printf("%-10s → %s%n", password, hash);
        }
        
        System.out.println("════════════════════════════════════════════════════════════════");
        System.out.println();
        System.out.println("Use these hashes to test the brute-force system!");
        System.out.println("Note: Character set is a-z and 0-9 only.");
        
        // Additional verification
        System.out.println();
        System.out.println("Verification Examples:");
        System.out.println("════════════════════════════════════════════════════════════════");
        
        // Quick tests
        String[] quickTests = {"abc", "test", "xyz"};
        for (String pwd : quickTests) {
            String hash = MD5Util.md5(pwd);
            System.out.printf("Password: '%s' (length %d)%n", pwd, pwd.length());
            System.out.printf("MD5 Hash: %s%n", hash);
            System.out.println();
        }
    }
}
