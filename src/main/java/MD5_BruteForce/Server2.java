package MD5_BruteForce;

import java.util.ArrayList;
import java.util.List;

public class Server2 {

	// this server will make first character search from 80 to 127
	static List<Search_Thread> threads = new ArrayList<Search_Thread>();
	// testing
	
	// Optimized thread startup for better performance with multiple threads
	public static void start_threads(String hashcode,List<String> ints) {
		// Create all threads first
		for(String inte:ints) {
			Search_Thread st = new Search_Thread(inte,hashcode,2);
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
		//System.out.println("server 2 stopped");
		// also servers shall stop
	}
	
	// method to start the server
	public static void start_server(String hashcode,int n) {
		threads.clear(); // Clear old threads
		List<String> intervals = intervals(n);
		start_threads(hashcode,intervals);
	}
	
	// Optimized interval distribution for even load balancing with up to 10 threads
	public static List<String> intervals(int n) {
		List<String> inter = new ArrayList<String>();
		int start = 80;
		int end = 127;
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
}

