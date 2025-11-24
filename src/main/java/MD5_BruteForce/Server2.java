package MD5_BruteForce;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Server2 handles the second half of ASCII range (80-126)
 * Similar to Server1 but different range
 */
public class Server2 {

	// Keep track of all threads for this server
	static List<Search_Thread> threads = new ArrayList<Search_Thread>();

	// Server2's ASCII range: P through ~ (tilde)
	public static final int START = 80; // 'P'
	public static final int END = 127; // DEL (exclusive)
	public static final int BASE = 94; // same base for calculation purposes
	public static final int BASE_OFFSET = 33; // offset for base-94 encoding
	public static final int CHUNK_SIZE = 1024;
	public static AtomicLong[] counters = new AtomicLong[7]; 
	public static long[] totals = new long[7];
	
	
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
	public static void start_server(String hashcode,int n) {
		// System.out.println("Server 2 starting with " + n + " threads"); // debug
		threads.clear(); // remove old threads
		// Calculate how many passwords this server needs to check
		int range = END - START;
		// Setup totals and counters for each length
		for (int len = 1; len <= 6; len++) {
			if (len == 1) {
				totals[len] = range;
			} else {
				// range * BASE^(len-1)
				long suffix = pow(BASE, len - 1);
				totals[len] = range * suffix;
			}
			counters[len] = new AtomicLong(0L); // start from 0
		}
		// Calculate intervals and start threads
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

	// Power function (same as Server1)
	private static long pow(int base, int exp) {
		long r = 1L;
		for (int i = 0; i < exp; i++) r *= base;
		return r;
	}
}

