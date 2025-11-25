package MD5_BruteForce;

import java.util.Scanner;
import java.util.List;

/**
 * Main server class for MD5 brute force attack
 */
public class Main_Server {
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		// System.out.println("DEBUG: Starting main server..."); // used for testing

		// Get the MD5 hash from user
		String hashcode;
		// Keep asking until we get valid input
		while (true) {
			System.out.print("Enter the hashcode --> ");
			hashcode = sc.nextLine();
			if (hashcode == null) { // check for null input
				
				System.out.println();
				System.out.println("No input detected. Exiting.");
				waitForExit(sc);
				sc.close();
				return;
			}
			hashcode = hashcode.trim(); // remove extra spaces
			// MD5 hash should be exactly 32 characters and only contain hex digits
			if (hashcode.length() == 32 && hashcode.matches("[0-9a-fA-F]{32}")) {
				break; // valid hash, exit loop
			}
			System.err.println("Error: Invalid MD5 hash format. MD5 hashes must be 32 hexadecimal characters. Please try again.");
		}
		
		// Get number of threads to use
		int totalThreads;
		// Input validation loop for thread count
		while (true) {
			System.out.print("Enter the total number of threads to use (1-10) --> ");
			String threadsLine = sc.nextLine();
			if (threadsLine == null) {
				System.out.println();
				System.out.println("No input detected. Exiting.");
				waitForExit(sc);
				sc.close();
				return;
			}
			threadsLine = threadsLine.trim();
			// Try to convert string to integer
			try {
				totalThreads = Integer.parseInt(threadsLine);
			} catch (NumberFormatException nfe) { // if not a valid number
				System.err.println("Error: Invalid number format for total threads. Please try again.");
				continue;
			}
			// Make sure thread count is in valid range
			if (totalThreads < 1 || totalThreads > 10) {
				System.err.println("Error: Number of threads must be between 1 and 10 (inclusive). Please try again.");
				continue; // ask again
			}
			break; // valid input received
		}
		
		
		// Main execution loop - allows running multiple searches
		while (true) {
			System.out.println();
			System.out.println("Starting brute force attack...");
			System.out.println("Hash: " + hashcode);

			// Split threads between servers (Server1 gets more if odd number)
			int threadsServer1 = (totalThreads + 1) / 2;
			int threadsServer2 = totalThreads - threadsServer1;
			System.out.println("Total threads: " + totalThreads + " (Server1=" + threadsServer1 + ", Server2=" + threadsServer2 + ")");
			System.out.println();

			// Reset everything before starting new search
			SearchCoordinator.reset();
			GlobalDistributor.init();
			SearchCoordinator.setStartTime(System.nanoTime()); // track start time for performance

			// Start both servers
			Server1.start_server(hashcode, threadsServer1);
			if (threadsServer2 > 0) { // only start server 2 if it has threads
				Server2.start_server(hashcode, threadsServer2);
			}

			// Wait for all threads to finish
			List<Thread> joinList = new java.util.ArrayList<>();
			// Collect all threads from both servers
			joinList.addAll(Server1.threads);
			joinList.addAll(Server2.threads);
			// Join all threads (wait for them to complete)
			for (Thread t : joinList) {
				try {
					t.join(); // this blocks until thread finishes
				} catch (InterruptedException e) {
					// Handle interruption properly
					Thread.currentThread().interrupt();
				}
			}

			System.out.println();
			System.out.println("Search finished.");

			// Ask if user wants to search another hash
			while (true) {
				System.out.print("Run another hash? (Y (Yes)/N (No)) ");
				String resp = sc.nextLine();
				if (resp == null) { // EOF or ctrl+d
					
					System.out.println();
					System.out.println("No input detected. Exiting.");
					waitForExit(sc);
					sc.close();
					return;
				}
				resp = resp.trim(); // clean up the response
				// Check if user wants to exit
				if (resp.isEmpty() || resp.equalsIgnoreCase("n") || resp.equalsIgnoreCase("no")) {
					
					System.out.println("Exiting program.");
					waitForExit(sc);
					sc.close();
					return;
				}
				// If they said yes, continue to get new hash
				if (resp.equalsIgnoreCase("y") || resp.equalsIgnoreCase("yes")) {
					
					break; // exit this loop, continue to get new hash
				}
				// Invalid response, ask again
				System.out.println("Please answer 'y' or 'n'.");
			}

			// Get the new hash to search
			String newHash;
			while (true) {
				System.out.print("Enter the hashcode --> ");
				newHash = sc.nextLine();
				if (newHash == null) {
					System.out.println();
					System.out.println("No input detected. Exiting.");
					waitForExit(sc);
					sc.close();
					return;
				}
				newHash = newHash.trim();
				if (newHash.length() == 32 && newHash.matches("[0-9a-fA-F]{32}")) {
					break;
				}
				System.err.println("Error: Invalid MD5 hash format. MD5 hashes must be 32 hexadecimal characters. Please try again.");
			}
			hashcode = newHash; // update the hashcode for next iteration

			// Get new thread count for next search
			while (true) {
				System.out.print("Enter the total number of threads to use (1-10) --> ");
				String threadsLine = sc.nextLine();
				if (threadsLine == null) {
					System.out.println();
					System.out.println("No input detected. Exiting.");
					waitForExit(sc);
					sc.close();
					return;
				}
				threadsLine = threadsLine.trim();
				try {
					totalThreads = Integer.parseInt(threadsLine);
				} catch (NumberFormatException nfe) {
					System.err.println("Error: Invalid number format for total threads. Please try again.");
					continue;
				}
				if (totalThreads < 1 || totalThreads > 10) {
					System.err.println("Error: Number of threads must be between 1 and 10 (inclusive). Please try again.");
					continue;
				}
				break;
			}
		}
	} // end of main method

	/**
	 * Helper method to wait for user to press enter before closing
	 * This prevents the console window from closing immediately
	 */
	private static void waitForExit(Scanner sc) {
		System.out.println();
		System.out.println("Press Enter to quit.");
		// Keep waiting until user presses enter
		while (true) {
			String line = sc.nextLine();
			if (line == null) break; // EOF
			line = line.trim();
			if (line.isEmpty() || line.equalsIgnoreCase("exit")) break; // exit conditions
			System.out.println("Type 'exit' or press Enter to quit.");
		}
	}
}

