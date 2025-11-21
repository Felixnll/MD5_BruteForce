package MD5_BruteForce;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Server2 {

	
	static List<Search_Thread> threads = new ArrayList<Search_Thread>();

	
	public static final int START = 80;
	public static final int END = 127; 
	public static final int BASE = 94; 
	public static final int BASE_OFFSET = 33;
	public static final int CHUNK_SIZE = 1024;
	public static AtomicLong[] counters = new AtomicLong[7]; 
	public static long[] totals = new long[7];
	
	
	
	public static void start_threads(String hashcode,List<String> ints) {
		
		for(String inte:ints) {
			Search_Thread st = new Search_Thread(inte,hashcode,2);
			threads.add(st);
		}
		
		for(Search_Thread st:threads) {
			st.start();
		}
	}
	
	
	public static void stop_threads() {
		for(Search_Thread st:threads) {
			st.setStop(true);
		}
	}
	
	public static void start_server(String hashcode,int n) {
		threads.clear();
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
	
	
	public static List<String> intervals(int n) {
		List<String> inter = new ArrayList<String>();
		int start = START;
		int end = END;
		int totalRange = end - start;
		
		
		int baseInterval = totalRange / n;
		int remainder = totalRange % n;
		
		int currentStart = start;
		for (int i = 0; i < n; i++) {
			int intervalSize = baseInterval;
			
			if (i < remainder) {
				intervalSize++;
			}
			int currentEnd = currentStart + intervalSize;
			
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

