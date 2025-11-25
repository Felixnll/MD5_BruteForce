package MD5_BruteForce;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.DigestException;

/**
 * Worker thread that searches for passwords within a given range
 * Each thread gets assigned an interval to search through
 */
public class Search_Thread extends Thread{
	
	String interval; // the range this thread is searching
	static int cmp = 0; // counter for thread IDs
	int id = 0; // unique ID for this thread
	volatile boolean stop = false; // flag to stop the thread
	String hashcode; // target MD5 hash
	int server; // which server this thread belongs to

	private MessageDigest md; // reusable MD5 digest instance
	private byte[] inputBytes = new byte[6]; // buffer for password being tested
	private byte[] targetDigest; // the hash we're trying to match
	private byte[] digestBuffer = new byte[16]; // buffer for computed hash 
	
	// Constructor
	public Search_Thread(String i,String hash,int s) {
		interval = i;
		id = cmp; // assign unique ID
		hashcode = hash;
		server = s;
		cmp++; // increment for next thread
		// Initialize MD5 digest
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// This shouldn't happen since MD5 is always available
			throw new RuntimeException(e);
		}
		targetDigest = hexStringToByteArray(hashcode); // convert target hash to bytes

		
		// Print debug info about this thread's assigned interval
		try {
			String[] parts = interval.split("-");
			int a = Integer.parseInt(parts[0]);
			int b = Integer.parseInt(parts[1]);
			char ca = (char) a; // convert to char to see what it looks like
			char cb = (char) (b - 1);
			this.setName("S" + server + "-T" + id); // set thread name for debugging
			System.out.println
			("Thread " + id + " (Server " + server + ") assigned interval " + interval + " -> ascii [" + a + ".." + (b-1) + "] ('" + ca + "'..'" + cb + "')");
		} catch (Exception ex) {
			// Ignore if interval format is wrong
		}
	}


	// Method to signal this thread to stop searching
	public void setStop(boolean b) {
		// Only print message first time we're stopped
		if (!stop && b) {
			stop = true;
			System.out.println("Thread "+id+" stopped");
		} else {
			// Already stopped or not stopping
			stop = stop || b;
		}
	}
	

	/**
	 * Check if the current password in inputBytes matches the target hash
	 * This is called thousands of times per second so needs to be fast
	 */
	private boolean md5Matches(int length) {
		md.reset(); // reset digest for new calculation // reset digest for new calculation
		// Compute MD5 hash of current password attempt
		byte[] digest;
		try {
			// Update with the bytes we want to hash
			md.update(inputBytes, 0, length);
			// Try to use the buffer to avoid allocation
			int written = md.digest(digestBuffer, 0, digestBuffer.length);
			if (written != digestBuffer.length) {
				digest = new byte[written];
				System.arraycopy(digestBuffer, 0, digest, 0, written);
			} else {
				digest = digestBuffer;
			}
		} catch (DigestException de) {
			// Fallback if buffer method doesn't work
			digest = md.digest(inputBytes);
		}
		// Compare computed hash with target (constant-time comparison)
		return MessageDigest.isEqual(digest, targetDigest);
	}
	
	// Helper method to convert hex string to byte array
	private static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			int hi = Character.digit(s.charAt(i), 16);
			int lo = Character.digit(s.charAt(i + 1), 16);
			data[i / 2] = (byte) ((hi << 4) + lo);
		}
		return data;
	}

	// Main thread execution - search passwords of length 1 to 6
	public void run() {
		// Try each password length from 1 to 6
		for (int len = 1; len <= 6 && !SearchCoordinator.isFound() && !stop; len++) {
			System.out.println("Thread " + id + " on Server " + server + " searching length " + len);
			// Process this length using dynamic work distribution
			processLengthDynamic(len, GlobalDistributor.START, 
									GlobalDistributor.END, 
									GlobalDistributor.BASE, 
									GlobalDistributor.BASE_OFFSET, 
									GlobalDistributor.CHUNK_SIZE, 
									GlobalDistributor.counters, 
									GlobalDistributor.totals);
		}
		// System.out.println("Thread " + id + " finished all lengths"); // debug
	}

	private void foundPassword(String password) {
		SearchCoordinator.reportFound(password, id, server);
	}

	private void processLengthDynamic(int length, 
									int serverStart, 
									int serverEnd, 
									int base, 
									int baseOffset, 
									int chunkSize, 
									java.util.concurrent.atomic.AtomicLong[] countersArr, 
									long[] totalsArr)
{
		long total = totalsArr[length];
		if (total <= 0) return;

		long suffixTotal = 1L;
		if (length > 1) {
			for (int i = 0; i < length - 1; i++) suffixTotal *= base;
		}

		java.util.concurrent.atomic.AtomicLong counter = countersArr[length];
		while (!SearchCoordinator.isFound() && !stop) {
			long start = counter.getAndAdd(chunkSize);
			if (start >= total) break;
			long end = Math.min(start + chunkSize, total);
			for (long idx = start; idx < end && !SearchCoordinator.isFound() && !stop; idx++) {
				if (length == 1) {
					int ch = serverStart + (int) idx;
					inputBytes[0] = (byte) ch;
				} else {
					long firstOffset = idx / suffixTotal;
					long suffixIdx = idx % suffixTotal;
					inputBytes[0] = (byte) (serverStart + (int) firstOffset);
					long rem = suffixIdx;
					for (int pos = length - 1; pos >= 1; pos--) {
						int digit = (int) (rem % base);
						inputBytes[pos] = (byte) (baseOffset + digit);
						rem /= base;
					}
				}

				if (md5Matches(length)) {
					String pwd = new String(inputBytes, 0, length);
					foundPassword(pwd);
					return;
				}
			}
		}
	}
}