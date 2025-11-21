package MD5_BruteForce;

import java.util.Scanner;
import java.util.List;

public class Main_Server {
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		
		System.out.print("Enter the hashcode --> ");
		String hashcode = sc.next().trim();
		
		
		if (hashcode.length() != 32 || !hashcode.matches("[0-9a-fA-F]{32}")) {
			System.err.println("Error: Invalid MD5 hash format. MD5 hashes must be 32 hexadecimal characters.");
			sc.close();
			return;
		}
		
		
		System.out.print("Enter the total number of threads to use (1-10) --> ");
		int totalThreads = sc.nextInt();
		
	
		if (totalThreads < 1 || totalThreads > 10) {
			System.err.println("Error: Number of threads must be between 1 and 10 (inclusive).");
			sc.close();
			return;
		}
		
		sc.close();
		
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
		
	}
}

