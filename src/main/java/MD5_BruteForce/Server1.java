package MD5_BruteForce;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Server1 handles the first half of the ASCII range (33-79)
 * Works together with Server2 to split the workload
 */
public class Server1 {
	// List to keep track of all threads created by this server
	static List<Search_Thread> threads = new ArrayList<Search_Thread>();

	// ASCII range for Server1: ! through O
	public static final int START = 33; // '!'
	public static final int END = 80; // 'P' (exclusive)
	public static final int BASE = 94; // total characters across both servers
	public static final int BASE_OFFSET = 33;
	public static final int CHUNK_SIZE = 1024; // passwords per work chunk
	public static AtomicLong[] counters = new AtomicLong[7]; // one for each length
	public static long[] totals = new long[7]; // total combinations per length
	
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
	public static void start_server(String hashcode,int n) {
		System.out.println("Server 1 starting..."); // changed from "Server Start" for clarity
		threads.clear(); // clear any old threads from previous runs
		// Calculate search space for this server's range
		int range = END - START; // how many first chars this server handles
		// Pre-calculate totals for each password length
		for (int len = 1; len <= 6; len++) {
			if (len == 1) {
				totals[len] = range; // just first character
			} else {
				// For multi-char passwords: first_char_range * (all_chars ^ remaining_positions)
				long suffix = pow(BASE, len - 1);
				totals[len] = range * suffix;
			}
			counters[len] = new AtomicLong(0L); // reset counter
		}
		// Divide work among n threads and start them
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

	// Helper: calculate base^exp (couldn't use Math.pow since it returns double)
	private static long pow(int base, int exp) {
		long r = 1L;
		for (int i = 0; i < exp; i++) r *= base; // simple iterative power
		return r;
	}
}

