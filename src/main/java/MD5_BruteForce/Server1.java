package MD5_BruteForce;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Server1 {
	
	// this server will make first character search from 33 to 80
	static List<Search_Thread> threads = new ArrayList<Search_Thread>();

	// dynamic chunking support
	public static final int START = 33;
	public static final int END = 80; // exclusive
	public static final int BASE = 94; // characters per position (33..126 inclusive)
	public static final int BASE_OFFSET = 33;
	public static final int CHUNK_SIZE = 1024;
	public static AtomicLong[] counters = new AtomicLong[7]; // index by length 1..6
	public static long[] totals = new long[7];
	
	
	// Optimized thread startup for better performance with multiple threads
	public static void start_threads(String hashcode,List<String> ints) {
		// Create all threads first
		for(String inte:ints) {
			Search_Thread st = new Search_Thread(inte,hashcode,1);
			threads.add(st);
		}
		// Start all threads - this allows JVM to optimize thread scheduling
		for(Search_Thread st:threads) {
			st.start();
		}
	}
	
	// method to stop threads
	public static void stop_threads() {
		for(Search_Thread st:threads) {
			st.setStop(true);
		}
		//System.out.println("server 1 stopped");
		// also servers shall stop
	}
	
	// method to start the server
	public static void start_server(String hashcode,int n) {
		System.out.println("Server Start");
		threads.clear(); // Clear old threads
		// initialize dynamic counters/totals per length
		int range = END - START;
		for (int len = 1; len <= 6; len++) {
			if (len == 1) {
				totals[len] = range;
			} else {
				long suffix = pow(BASE, len - 1);
				totals[len] = range * suffix;
			}
			counters[len] = new AtomicLong(0L);
		}
		List<String> intervals = intervals(n);
		start_threads(hashcode,intervals);
	}
	
	// Optimized interval distribution for even load balancing with up to 10 threads
	public static List<String> intervals(int n) {
		List<String> inter = new ArrayList<String>();
		int start = START;
		int end = END;
		int totalRange = end - start;
		
		// Calculate base interval size and remainder for perfect distribution
		int baseInterval = totalRange / n;
		int remainder = totalRange % n;
		
		int currentStart = start;
		for (int i = 0; i < n; i++) {
			int intervalSize = baseInterval;
			// Distribute remainder across first few threads for perfect balance
			if (i < remainder) {
				intervalSize++;
			}
			int currentEnd = currentStart + intervalSize;
			// Ensure last interval ends exactly at 'end'
			if (i == n - 1) {
				currentEnd = end;
			}
			inter.add(currentStart + "-" + currentEnd);
			currentStart = currentEnd;
		}
		
		return inter;
	}

	private static long pow(int base, int exp) {
		long r = 1L;
		for (int i = 0; i < exp; i++) r *= base;
		return r;
	}
}

