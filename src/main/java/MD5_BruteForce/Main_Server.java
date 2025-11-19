package MD5_BruteForce;

import java.util.Scanner;
import java.util.List;

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
		
		// getting the number of threads (total across both servers)
		System.out.print("Enter the total number of threads to use (1-10) --> ");
		int totalThreads = sc.nextInt();
		
		// Validate thread count (total allowed threads 1..10)
		if (totalThreads < 1 || totalThreads > 10) {
			System.err.println("Error: Number of threads must be between 1 and 10 (inclusive).");
			sc.close();
			return;
		}
		
		sc.close();
		
		System.out.println();
		System.out.println("Starting brute force attack...");
		System.out.println("Hash: " + hashcode);
		// split threads between two servers (give extra to server1 if odd)
		int threadsServer1 = (totalThreads + 1) / 2;
		int threadsServer2 = totalThreads - threadsServer1;
		System.out.println("Total threads: " + totalThreads + " (Server1=" + threadsServer1 + ", Server2=" + threadsServer2 + ")");
		System.out.println();
		
		// Reset coordinator, distributor and set a common start time before starting threads
		SearchCoordinator.reset();
		GlobalDistributor.init();
		SearchCoordinator.setStartTime(System.nanoTime());

		// the search of the first character will be divided for the 2 servers
		// server one will get first character search from 33 to 80
		Server1.start_server(hashcode, threadsServer1);
		// server two will get first character search from 80 to 127
		if (threadsServer2 > 0) {
			Server2.start_server(hashcode, threadsServer2);
		}

		// wait for all threads to complete or for coordinator to report found
		// make local copies of thread lists to avoid concurrent modification
		List<Thread> joinList = new java.util.ArrayList<>();
		joinList.addAll(Server1.threads);
		joinList.addAll(Server2.threads);
		for (Thread t : joinList) {
			try {
				t.join();
			} catch (InterruptedException e) {
				// restore interrupt status
				Thread.currentThread().interrupt();
			}
		}
		
	}
}

