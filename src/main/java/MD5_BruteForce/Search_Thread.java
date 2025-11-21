package MD5_BruteForce;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.DigestException;

public class Search_Thread extends Thread{
	
	String interval;
	static int cmp = 0;
	int id = 0;
	volatile boolean stop = false;
	String hashcode;
	int server;

	private MessageDigest md;
	private byte[] inputBytes = new byte[6]; 
	private byte[] targetDigest; 
	private byte[] digestBuffer = new byte[16]; 
	
	public Search_Thread(String i,String hash,int s) {
		interval = i;
		id = cmp;
		hashcode = hash;
		server = s;
		cmp++;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		targetDigest = hexStringToByteArray(hashcode);

		
		try {
			String[] parts = interval.split("-");
			int a = Integer.parseInt(parts[0]);
			int b = Integer.parseInt(parts[1]);
			char ca = (char) a;
			char cb = (char) (b - 1);
			this.setName("S" + server + "-T" + id);
			System.out.println("Thread " + id + " (Server " + server + ") assigned interval " + interval + " -> ascii [" + a + ".." + (b-1) + "] ('" + ca + "'..'" + cb + "')");
		} catch (Exception ex) {
			
		}
	}


	
	public void setStop(boolean b) {
		
		if (!stop && b) {
			stop = true;
			System.out.println("Thread "+id+" stopped");
		} else {
			
			stop = stop || b;
		}
	}
	

	
	private boolean md5Matches(int length) {
		md.reset();
		
		byte[] digest;
		try {
			md.update(inputBytes, 0, length);
			int written = md.digest(digestBuffer, 0, digestBuffer.length);
			if (written != digestBuffer.length) {
				digest = new byte[written];
				System.arraycopy(digestBuffer, 0, digest, 0, written);
			} else {
				digest = digestBuffer;
			}
		} catch (DigestException de) {
			digest = md.digest(inputBytes);
		}
		return MessageDigest.isEqual(digest, targetDigest);
	}
	
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

	public void run() {
		for (int len = 1; len <= 6 && !SearchCoordinator.isFound() && !stop; len++) {
			System.out.println("Thread " + id + " on Server " + server + " searching length " + len);
			processLengthDynamic(len, GlobalDistributor.START, GlobalDistributor.END, GlobalDistributor.BASE, GlobalDistributor.BASE_OFFSET, GlobalDistributor.CHUNK_SIZE, GlobalDistributor.counters, GlobalDistributor.totals);
		}
	}

	private void foundPassword(String password) {
		SearchCoordinator.reportFound(password, id, server);
	}

	private void processLengthDynamic(int length, int serverStart, int serverEnd, int base, int baseOffset, int chunkSize, java.util.concurrent.atomic.AtomicLong[] countersArr, long[] totalsArr) {
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