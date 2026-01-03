package MD5_BruteForce;

import java.util.ArrayList;
import java.util.List;

/**
 * Server2 handles the second half of ASCII range (80-126)
 * Works together with Server1 to split the workload
 * 
 * STATIC PARTITIONING:
 * - Server2 owns first-character range [80, 127) = 47 chars ('P' to '~')
 * - Each thread within Server2 gets a non-overlapping sub-range
 * - Threads search ALL suffix combinations for their assigned first-chars
 * - NO overlap with Server1 (which handles [33, 80))
 */
public class Server2 {

	// Keep track of all threads for this server
	static List<Search_Thread> threads = new ArrayList<Search_Thread>();

	// Server2's ASCII range: P through ~ (STATIC, NON-OVERLAPPING with Server1)
	public static final int START = 80; // 'P' - inclusive (Server1 ends here)
	public static final int END = 127; // DEL - exclusive
	public static final int BASE = 94; // same base for calculation purposes
	
	
	// Initialize and launch all threads
	public static void start_threads(String hashcode,List<String> ints) {
		// Create thread objects first
		for(String inte:ints) {
			Search_Thread st = new Search_Thread(inte,hashcode,2); // 2 = Server2 ID
			threads.add(st);
		}
		// Start all threads
		for(Search_Thread st:threads) {
			st.start();
		}
	}
	
	// Tell all threads to stop searching
	public static void stop_threads() {
		for(Search_Thread st:threads) {
			st.setStop(true); // sets the stop flag
		}
	}
	
	// Start Server2 with the specified number of threads
	// Uses STATIC PARTITIONING - divides first-char range among threads
	public static void start_server(String hashcode,int n) {
		System.out.println("Server 2 starting with " + n + " thread(s)...");
		System.out.println("Server 2 range: [" + START + ", " + END + ") -> '" + (char)START + "' to '" + (char)(END-1) + "'");
		threads.clear(); // remove old threads
		
		// STATIC PARTITIONING: Divide first-char range among n threads
		// Each thread gets a non-overlapping sub-range of first characters
		List<String> intervals = intervals(n);
		start_threads(hashcode,intervals);
	}
	
	// Divide the range into n intervals (one per thread)
	public static List<String> intervals(int n) {
		List<String> inter = new ArrayList<String>();
		int start = START;
		int end = END;
		int totalRange = end - start;
		
		// Split range as evenly as possible
		int baseInterval = totalRange / n;
		int remainder = totalRange % n; // extra chars to distribute
		
		int currentStart = start;
		for (int i = 0; i < n; i++) {
			int intervalSize = baseInterval;
			// Give extra character to first 'remainder' threads
			if (i < remainder) {
				intervalSize++; // make it more balanced
			}
			int currentEnd = currentStart + intervalSize;
			// Ensure last thread ends exactly at END
			if (i == n - 1) {
				currentEnd = end;
			}
			inter.add(currentStart + "-" + currentEnd);
			currentStart = currentEnd; // move to next interval
		}
		
		return inter;
	}
}
