package MD5_BruteForce;

import java.util.Scanner;
import java.util.List;

public class Main_Server {
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);


		String hashcode;
		while (true) {
			System.out.print("Enter the hashcode --> ");
			hashcode = sc.nextLine();
			if (hashcode == null) {
				
				System.out.println();
				System.out.println("No input detected. Exiting.");
				waitForExit(sc);
				sc.close();
				return;
			}
			hashcode = hashcode.trim();
			if (hashcode.length() == 32 && hashcode.matches("[0-9a-fA-F]{32}")) {
				break;
			}
			System.err.println("Error: Invalid MD5 hash format. MD5 hashes must be 32 hexadecimal characters. Please try again.");
		}
		
		
		int totalThreads;
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
		
		
		while (true) {
			System.out.println();
			System.out.println("Starting brute force attack...");
			System.out.println("Hash: " + hashcode);

			int threadsServer1 = (totalThreads + 1) / 2;
			int threadsServer2 = totalThreads - threadsServer1;
			System.out.println("Total threads: " + totalThreads + " (Server1=" + threadsServer1 + ", Server2=" + threadsServer2 + ")");
			System.out.println();

			SearchCoordinator.reset();
			GlobalDistributor.init();
			SearchCoordinator.setStartTime(System.nanoTime());

			Server1.start_server(hashcode, threadsServer1);
			if (threadsServer2 > 0) {
				Server2.start_server(hashcode, threadsServer2);
			}

			List<Thread> joinList = new java.util.ArrayList<>();
			joinList.addAll(Server1.threads);
			joinList.addAll(Server2.threads);
			for (Thread t : joinList) {
				try {
					t.join();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}

			System.out.println();
			System.out.println("Search finished.");

			
			while (true) {
				System.out.print("Run another hash? (Y (Yes)/N (No)) ");
				String resp = sc.nextLine();
				if (resp == null) {
					
					System.out.println();
					System.out.println("No input detected. Exiting.");
					waitForExit(sc);
					sc.close();
					return;
				}
				resp = resp.trim();
				if (resp.isEmpty() || resp.equalsIgnoreCase("n") || resp.equalsIgnoreCase("no")) {
					
					System.out.println("Exiting program.");
					waitForExit(sc);
					sc.close();
					return;
				}
				if (resp.equalsIgnoreCase("y") || resp.equalsIgnoreCase("yes")) {
					
					break;
				}
				System.out.println("Please answer 'y' or 'n'.");
			}

			
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
			hashcode = newHash;

			// get new thread count
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
	}

	
	private static void waitForExit(Scanner sc) {
		System.out.println();
		System.out.println("Press Enter to quit.");
		while (true) {
			String line = sc.nextLine();
			if (line == null) break;
			line = line.trim();
			if (line.isEmpty() || line.equalsIgnoreCase("exit")) break;
			System.out.println("Type 'exit' or press Enter to quit.");
		}
	}
}

