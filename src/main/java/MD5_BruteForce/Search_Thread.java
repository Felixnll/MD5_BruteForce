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
	private byte[] inputBytes = new byte[6]; // Reusable buffer
	private byte[] targetDigest; // Parsed target hash (16 bytes)
	private byte[] digestBuffer = new byte[16]; // Reusable digest buffer
	
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

		// Log assigned interval for traceability (helpful for PPT / debugging)
		try {
			String[] parts = interval.split("-");
			int a = Integer.parseInt(parts[0]);
			int b = Integer.parseInt(parts[1]);
			// store parsed interval endpoints for potential use
			this.startByte = a;
			this.endByte = b;
			char ca = (char) a;
			char cb = (char) (b - 1);
			this.setName("S" + server + "-T" + id);
			System.out.println("Thread " + id + " (Server " + server + ") assigned interval " + interval + " -> ascii [" + a + ".." + (b-1) + "] ('" + ca + "'..'" + cb + "')");
		} catch (Exception ex) {
			// ignore logging errors
		}
	}

	private int startByte = 0;
	private int endByte = 0;
	
	public void setStop(boolean b) {
		// Print stopped message only the first time we transition to stopped
		if (!stop && b) {
			stop = true;
			System.out.println("Thread "+id+" stopped");
		} else {
			// Ensure stop is set if requested but avoid duplicate prints
			stop = stop || b;
		}
	}
	

	
	private boolean md5Matches(int length) {
		md.reset();
		// Use digest(byte[] out, int outOffset, int outLen) when available to avoid allocation
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
		String[] A = interval.split("-");
		int a = Integer.parseInt(A[0]);
		int b = Integer.parseInt(A[1]);

		// Search by length: 1-char, then 2-char, then 3-char, etc.
		for (int len = 1; len <= 6 && !SearchCoordinator.isFound() && !stop; len++) {
			System.out.println("Thread " + id + " on Server " + server + " searching length " + len);
			// Use global distributor for better load balance across threads
			processLengthDynamic(len, GlobalDistributor.START, GlobalDistributor.END, GlobalDistributor.BASE, GlobalDistributor.BASE_OFFSET, GlobalDistributor.CHUNK_SIZE, GlobalDistributor.counters, GlobalDistributor.totals);
		}
	}

	private void searchLength(int length, int a, int b) {
		if (length == 1) {
			search1Char(a, b);
		} else if (length == 2) {
			search2Char(a, b);
		} else if (length == 3) {
			search3Char(a, b);
		} else if (length == 4) {
			search4Char(a, b);
		} else if (length == 5) {
			search5Char(a, b);
		} else if (length == 6) {
			search6Char(a, b);
		}
	}

	private void search1Char(int a, int b) {
		for (int i = a; i < b && !SearchCoordinator.isFound() && !stop; i++) {
			inputBytes[0] = (byte) i;
			if (md5Matches(1)) {
				foundPassword(new String(inputBytes, 0, 1));
				return;
			}
		}
	}

	private void search2Char(int a, int b) {
		for (int i = a; i < b && !SearchCoordinator.isFound() && !stop; i++) {
			inputBytes[0] = (byte) i;
			for (int j = 33; j < 127 && !SearchCoordinator.isFound() && !stop; j++) {
				inputBytes[1] = (byte) j;
				if (md5Matches(2)) {
					foundPassword(new String(inputBytes, 0, 2));
					return;
				}
			}
		}
	}

	private void search3Char(int a, int b) {
		for (int i = a; i < b && !SearchCoordinator.isFound() && !stop; i++) {
			inputBytes[0] = (byte) i;
			for (int j = 33; j < 127 && !SearchCoordinator.isFound() && !stop; j++) {
				inputBytes[1] = (byte) j;
				for (int k = 33; k < 127 && !SearchCoordinator.isFound() && !stop; k++) {
					inputBytes[2] = (byte) k;
					if (md5Matches(3)) {
						foundPassword(new String(inputBytes, 0, 3));
						return;
					}
				}
			}
		}
	}

	private void search4Char(int a, int b) {
		int checkCounter = 0;
		final int CHECK_INTERVAL = 1000;
		
		for (int i = a; i < b && !SearchCoordinator.isFound() && !stop; i++) {
			inputBytes[0] = (byte) i;
			for (int j = 33; j < 127 && !SearchCoordinator.isFound() && !stop; j++) {
				inputBytes[1] = (byte) j;
				for (int k = 33; k < 127 && !SearchCoordinator.isFound() && !stop; k++) {
					inputBytes[2] = (byte) k;
					for (int l = 33; l < 127 && !SearchCoordinator.isFound() && !stop; l++) {
						inputBytes[3] = (byte) l;
						if (md5Matches(4)) {
							foundPassword(new String(inputBytes, 0, 4));
							return;
						}
						if (++checkCounter >= CHECK_INTERVAL) {
							checkCounter = 0;
							if (SearchCoordinator.isFound() || stop) return;
						}
					}
				}
			}
		}
	}

	private void search5Char(int a, int b) {
		int checkCounter = 0;
		final int CHECK_INTERVAL = 1000;
		
		for (int i = a; i < b && !SearchCoordinator.isFound() && !stop; i++) {
			inputBytes[0] = (byte) i;
			for (int j = 33; j < 127 && !SearchCoordinator.isFound() && !stop; j++) {
				inputBytes[1] = (byte) j;
				for (int k = 33; k < 127 && !SearchCoordinator.isFound() && !stop; k++) {
					inputBytes[2] = (byte) k;
					for (int l = 33; l < 127 && !SearchCoordinator.isFound() && !stop; l++) {
						inputBytes[3] = (byte) l;
						for (int m = 33; m < 127 && !SearchCoordinator.isFound() && !stop; m++) {
							inputBytes[4] = (byte) m;
							if (md5Matches(5)) {
								foundPassword(new String(inputBytes, 0, 5));
								return;
							}
							if (++checkCounter >= CHECK_INTERVAL) {
								checkCounter = 0;
								if (SearchCoordinator.isFound() || stop) return;
							}
						}
					}
				}
			}
		}
	}

	private void search6Char(int a, int b) {
		int checkCounter = 0;
		final int CHECK_INTERVAL = 1000;
		
		for (int i = a; i < b && !SearchCoordinator.isFound() && !stop; i++) {
			inputBytes[0] = (byte) i;
			for (int j = 33; j < 127 && !SearchCoordinator.isFound() && !stop; j++) {
				inputBytes[1] = (byte) j;
				for (int k = 33; k < 127 && !SearchCoordinator.isFound() && !stop; k++) {
					inputBytes[2] = (byte) k;
					for (int l = 33; l < 127 && !SearchCoordinator.isFound() && !stop; l++) {
						inputBytes[3] = (byte) l;
						for (int m = 33; m < 127 && !SearchCoordinator.isFound() && !stop; m++) {
							inputBytes[4] = (byte) m;
							for (int n = 33; n < 127 && !SearchCoordinator.isFound() && !stop; n++) {
								inputBytes[5] = (byte) n;
								if (md5Matches(6)) {
									foundPassword(new String(inputBytes, 0, 6));
									return;
								}
								if (++checkCounter >= CHECK_INTERVAL) {
									checkCounter = 0;
									if (SearchCoordinator.isFound() || stop) return;
								}
							}
						}
					}
				}
			}
		}
	}

	private void foundPassword(String password) {
		// Delegate reporting and coordinated stopping to SearchCoordinator
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