package MD5_BruteForce;

import java.util.Scanner;

public class Main_Server {
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		// getting hash code from users
		System.out.print("Enter the hashcode --> ");
		String hashcode = sc.next().trim();
		
		// Validate hashcode format (MD5 hashes are 32 hex characters)
		if (hashcode.length() != 32 || !hashcode.matches("[0-9a-fA-F]{32}")) {
			System.err.println("Error: Invalid MD5 hash format. MD5 hashes must be 32 hexadecimal characters.");
			sc.close();
			return;
		}
		
		// getting the number of threads
		System.out.print("Enter the number of threads for each server --> ");
		int n = sc.nextInt();
		
		// Validate thread count
		if (n <= 0) {
			System.err.println("Error: Number of threads must be greater than 0.");
			sc.close();
			return;
		}
		if (n > 20) {
			System.err.println("Warning: Using more than 20 threads may not improve performance due to overhead.");
		}
		
		sc.close();
		
		System.out.println();
		System.out.println("Starting brute force attack...");
		System.out.println("Hash: " + hashcode);
		System.out.println("Threads per server: " + n);
		System.out.println();
		
		// the search of the first character will be divided for the 2 servers
		// server one will get first character search from 33 to 80
		Server1.start_server(hashcode, n);
	
		// server two will get first character search from 80 to 127
		Server2.start_server(hashcode, n);
		
	}
}

