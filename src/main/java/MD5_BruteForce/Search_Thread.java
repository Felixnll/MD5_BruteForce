package MD5_BruteForce;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.DigestException;

/**
 * Worker thread that searches for passwords within a given range
 * Each thread gets assigned a STATIC, NON-OVERLAPPING interval to search through
 * 
 * STATIC PARTITIONING SCHEME:
 * - Server 1 handles first-character range [33, 80) -> '!' to 'O'
 * - Server 2 handles first-character range [80, 127) -> 'P' to '~'
 * - Each thread within a server gets a sub-range of first characters
 * - Each thread searches ALL suffix combinations for its first-char range
 * - NO shared counters, NO dynamic work stealing - fully deterministic
 */
public class Search_Thread extends Thread{
	
	String interval; // the range this thread is searching (format: "start-end")
	static int cmp = 0; // counter for thread IDs
	int id = 0; // unique ID for this thread
	volatile boolean stop = false; // flag to stop the thread
	String hashcode; // target MD5 hash
	int server; // which server this thread belongs to

	// Static range boundaries parsed from interval
	private int firstCharStart; // inclusive start of first-character range
	private int firstCharEnd;   // exclusive end of first-character range

	private static final int BASE = 94; // total printable ASCII chars (33-126)
	private static final int BASE_OFFSET = 33; // offset for base-94 encoding

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

		// Parse the interval to get static first-character range
		try {
			String[] parts = interval.split("-");
			firstCharStart = Integer.parseInt(parts[0]); // inclusive
			firstCharEnd = Integer.parseInt(parts[1]);   // exclusive
			char ca = (char) firstCharStart;
			char cb = (char) (firstCharEnd - 1);
			this.setName("S" + server + "-T" + id); // set thread name for debugging
			System.out.println("Thread " + id + " (Server " + server + ") STATIC range: first-char [" 
				+ firstCharStart + ".." + (firstCharEnd-1) + "] ('" + ca + "'..'" + cb + "') - "
				+ (firstCharEnd - firstCharStart) + " first-chars assigned");
		} catch (Exception ex) {
			// Fallback if interval format is wrong
			firstCharStart = 33;
			firstCharEnd = 127;
			System.err.println("Thread " + id + " failed to parse interval: " + interval);
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
	// Uses STATIC partitioning - each thread searches its own first-char range
	public void run() {
		// Try each password length from 1 to 6
		for (int len = 1; len <= 6 && !SearchCoordinator.isFound() && !stop; len++) {
			long suffixCount = (len == 1) ? 1 : pow(BASE, len - 1);
			long totalForThisThread = (long)(firstCharEnd - firstCharStart) * suffixCount;
			System.out.println("Thread " + id + " on Server " + server + " searching length " + len 
				+ " (static range: " + totalForThisThread + " passwords)");
			// Process this length using STATIC work distribution (no shared counters)
			processLengthStatic(len);
		}
		if (!SearchCoordinator.isFound()) {
			System.out.println("Thread " + id + " on Server " + server + " completed all lengths (not found in range)");
		}
	}

	// Power function for calculating suffix combinations
	private static long pow(int base, int exp) {
		long r = 1L;
		for (int i = 0; i < exp; i++) r *= base;
		return r;
	}

	private void foundPassword(String password) {
		SearchCoordinator.reportFound(password, id, server);
	}

	/**
	 * STATIC partitioning: Process all passwords of given length where
	 * the first character is in this thread's assigned range [firstCharStart, firstCharEnd)
	 * 
	 * This method searches ALL suffix combinations for each first character.
	 * NO shared counters, NO dynamic work stealing - fully deterministic.
	 */
	private void processLengthStatic(int length) {
		// Calculate how many suffix combinations exist for this length
		long suffixTotal = 1L;
		if (length > 1) {
			suffixTotal = pow(BASE, length - 1);
		}

		// Iterate through each first character in our STATIC assigned range
		for (int firstChar = firstCharStart; firstChar < firstCharEnd && !SearchCoordinator.isFound() && !stop; firstChar++) {
			inputBytes[0] = (byte) firstChar;

			if (length == 1) {
				// Only first character, check immediately
				if (md5Matches(1)) {
					String pwd = new String(inputBytes, 0, 1);
					foundPassword(pwd);
					return;
				}
			} else {
				// Iterate through ALL suffix combinations for this first character
				// This is the key to static partitioning - each thread handles its own suffixes
				for (long suffixIdx = 0; suffixIdx < suffixTotal && !SearchCoordinator.isFound() && !stop; suffixIdx++) {
					// Convert suffixIdx to characters using base-94 encoding
					long rem = suffixIdx;
					for (int pos = length - 1; pos >= 1; pos--) {
						int digit = (int) (rem % BASE);
						inputBytes[pos] = (byte) (BASE_OFFSET + digit);
						rem /= BASE;
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
}