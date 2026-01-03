package MD5_BruteForce;

import java.util.ArrayList;
import java.util.List;

/**
 * Server1 handles the first half of the ASCII range (33-79)
 * Works together with Server2 to split the workload
 * 
 * STATIC PARTITIONING:
 * - Server1 owns first-character range [33, 80) = 47 chars ('!' to 'O')
 * - Each thread within Server1 gets a non-overlapping sub-range
 * - Threads search ALL suffix combinations for their assigned first-chars
 * - NO overlap with Server2 (which handles [80, 127))
 */
public class Server1 {
	// List to keep track of all threads created by this server
	static List<Search_Thread> threads = new ArrayList<Search_Thread>();

	// ASCII range for Server1: ! through O (STATIC, NON-OVERLAPPING with Server2)
	public static final int START = 33; // '!' - inclusive
	public static final int END = 80; // 'P' - exclusive (Server2 starts here)
	public static final int BASE = 94; // total characters across both servers
	
	// Create and start all threads for this server
	public static void start_threads(String hashcode,List<String> ints) {
		// First create all thread objects
		for(String inte:ints) {
			Search_Thread st = new Search_Thread(inte,hashcode,1); // 1 = server ID
			threads.add(st);
		}
		// Then start them all
		for(Search_Thread st:threads) {
			st.start(); // begins execution
		}
	}
	
	// Signal all threads to stop (called when password is found)
	public static void stop_threads() {
		for(Search_Thread st:threads) {
			st.setStop(true); // set stop flag for each thread
		}
	}
	
	// Main entry point to start Server1 with n threads
	// Uses STATIC PARTITIONING - divides first-char range among threads
	public static void start_server(String hashcode,int n) {
		System.out.println("Server 1 starting with " + n + " thread(s)...");
		System.out.println("Server 1 range: [" + START + ", " + END + ") -> '" + (char)START + "' to '" + (char)(END-1) + "'");
		threads.clear(); // clear any old threads from previous runs
		
		// STATIC PARTITIONING: Divide first-char range among n threads
		// Each thread gets a non-overlapping sub-range of first characters
		List<String> intervals = intervals(n);
		start_threads(hashcode,intervals);
	}
	
	/**
	 * Divide this server's ASCII range into n equal intervals
	 * This was tricky to get right - had to handle remainder properly
	 */
	public static List<String> intervals(int n) {
		List<String> inter = new ArrayList<String>();
		int start = START;
		int end = END;
		int totalRange = end - start; // total chars to divide
		
		// Divide as evenly as possible
		int baseInterval = totalRange / n; // base size for each thread
		int remainder = totalRange % n; // some threads get +1 if not evenly divisible
		
		int currentStart = start;
		for (int i = 0; i < n; i++) {
			int intervalSize = baseInterval;
			// First 'remainder' threads get an extra character
			if (i < remainder) {
				intervalSize++; // distribute remainder evenly
			}
			int currentEnd = currentStart + intervalSize;
			// Make sure last interval goes exactly to END
			if (i == n - 1) {
				currentEnd = end; // safety check
			}
			inter.add(currentStart + "-" + currentEnd); // format: "start-end"
			currentStart = currentEnd; // next interval starts where this one ends
		}
		
		return inter;
	}
}
