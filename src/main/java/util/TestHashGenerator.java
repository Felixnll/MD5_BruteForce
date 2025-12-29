package util;

/**
 * TestHashGenerator - Utility to generate MD5 hashes for testing
 * Use this to create test cases for the brute-force system
 */
public class TestHashGenerator {
    
    public static void main(String[] args) {
        System.out.println("+--------------------------------------------------------------+");
        System.out.println("|              MD5 HASH GENERATOR FOR TESTING                  |");
        System.out.println("|       Character Set: ASCII 33-126 (94 characters)            |");
        System.out.println("+--------------------------------------------------------------+");
        System.out.println();
        
        System.out.println("Character Set Used:");
        System.out.println(MD5Util.CHARSET);
        System.out.println("Total Characters: " + MD5Util.CHARSET_SIZE);
        System.out.println();
        
        // Generate hashes for test passwords
        String[] testPasswords = {
            "!",         // 1 character
            "AB",        // 2 characters
            "abc",       // 3 characters
            "Test",      // 4 characters (mixed case)
            "Hello",     // 5 characters
            "Pass!@",    // 6 characters with special
        };
        
        System.out.println("Password -> MD5 Hash");
        System.out.println("================================================================");
        
        for (String password : testPasswords) {
            String hash = MD5Util.md5(password);
            System.out.printf("%-10s -> %s%n", password, hash);
        }
        
        System.out.println();
        System.out.println("================================================================");
        System.out.println("GROUP 3 TARGET HASHES TO CRACK:");
        System.out.println("================================================================");
        System.out.println("2-char: 263a6fee6029b304bd1cf5ce0a782c6b");
        System.out.println("3-char: 77aaa4dcce557f10d97b3ed037de33fb");
        System.out.println("4-char: 9d64f0e38b080d131c1a27140df4e13b");
        System.out.println("5-char: e76b29d2dfffb1a327d49a797d34c8a7");
        System.out.println("6-char: f7808b86b6e53a97313f24a3619fdc95");
        System.out.println();
        
        System.out.println("Search Space Sizes:");
        System.out.println("================================================================");
        for (int len = 1; len <= 6; len++) {
            long space = MD5Util.calculateSearchSpace(len);
            System.out.printf("Length %d: %,15d combinations%n", len, space);
        }
    }
}
