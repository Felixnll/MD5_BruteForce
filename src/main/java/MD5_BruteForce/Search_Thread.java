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
	private static volatile boolean globalStop = false;
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
	}
	
	public void setStop(boolean b) {
		stop = b;
		globalStop = true;
		System.out.println("Thread "+id+" stopped");
	}
	
	public static void resetGlobalStop() {
		globalStop = false;
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
		
		long startTime = System.nanoTime();
		
		// Search by length: 1-char, then 2-char, then 3-char, etc.
		for (int len = 1; len <= 6 && !globalStop && !stop; len++) {
			System.out.println("Thread " + id + " on Server " + server + " searching length " + len);
			searchLength(len, a, b, startTime);
		}
	}
	
	private void searchLength(int length, int a, int b, long startTime) {
		if (length == 1) {
			search1Char(a, b, startTime);
		} else if (length == 2) {
			search2Char(a, b, startTime);
		} else if (length == 3) {
			search3Char(a, b, startTime);
		} else if (length == 4) {
			search4Char(a, b, startTime);
		} else if (length == 5) {
			search5Char(a, b, startTime);
		} else if (length == 6) {
			search6Char(a, b, startTime);
		}
	}
	
	private void search1Char(int a, int b, long startTime) {
		for (int i = a; i < b && !globalStop && !stop; i++) {
			inputBytes[0] = (byte) i;
			if (md5Matches(1)) {
				foundPassword(new String(inputBytes, 0, 1), startTime);
				return;
			}
		}
	}
	
	private void search2Char(int a, int b, long startTime) {
		for (int i = a; i < b && !globalStop && !stop; i++) {
			inputBytes[0] = (byte) i;
			for (int j = 33; j < 127 && !globalStop && !stop; j++) {
				inputBytes[1] = (byte) j;
				if (md5Matches(2)) {
					foundPassword(new String(inputBytes, 0, 2), startTime);
					return;
				}
			}
		}
	}
	
	private void search3Char(int a, int b, long startTime) {
		for (int i = a; i < b && !globalStop && !stop; i++) {
			inputBytes[0] = (byte) i;
			for (int j = 33; j < 127 && !globalStop && !stop; j++) {
				inputBytes[1] = (byte) j;
				for (int k = 33; k < 127 && !globalStop && !stop; k++) {
					inputBytes[2] = (byte) k;
					if (md5Matches(3)) {
						foundPassword(new String(inputBytes, 0, 3), startTime);
						return;
					}
				}
			}
		}
	}
	
	private void search4Char(int a, int b, long startTime) {
		int checkCounter = 0;
		final int CHECK_INTERVAL = 1000;
		
		for (int i = a; i < b && !globalStop && !stop; i++) {
			inputBytes[0] = (byte) i;
			for (int j = 33; j < 127 && !globalStop && !stop; j++) {
				inputBytes[1] = (byte) j;
				for (int k = 33; k < 127 && !globalStop && !stop; k++) {
					inputBytes[2] = (byte) k;
					for (int l = 33; l < 127; l++) {
						inputBytes[3] = (byte) l;
						if (md5Matches(4)) {
							foundPassword(new String(inputBytes, 0, 4), startTime);
							return;
						}
						if (++checkCounter >= CHECK_INTERVAL) {
							checkCounter = 0;
							if (globalStop || stop) return;
						}
					}
				}
			}
		}
	}
	
	private void search5Char(int a, int b, long startTime) {
		int checkCounter = 0;
		final int CHECK_INTERVAL = 1000;
		
		for (int i = a; i < b && !globalStop && !stop; i++) {
			inputBytes[0] = (byte) i;
			for (int j = 33; j < 127 && !globalStop && !stop; j++) {
				inputBytes[1] = (byte) j;
				for (int k = 33; k < 127 && !globalStop && !stop; k++) {
					inputBytes[2] = (byte) k;
					for (int l = 33; l < 127 && !globalStop && !stop; l++) {
						inputBytes[3] = (byte) l;
						for (int m = 33; m < 127; m++) {
							inputBytes[4] = (byte) m;
							if (md5Matches(5)) {
								foundPassword(new String(inputBytes, 0, 5), startTime);
								return;
							}
							if (++checkCounter >= CHECK_INTERVAL) {
								checkCounter = 0;
								if (globalStop || stop) return;
							}
						}
					}
				}
			}
		}
	}
	
	private void search6Char(int a, int b, long startTime) {
		int checkCounter = 0;
		final int CHECK_INTERVAL = 1000;
		
		for (int i = a; i < b && !globalStop && !stop; i++) {
			inputBytes[0] = (byte) i;
			for (int j = 33; j < 127 && !globalStop && !stop; j++) {
				inputBytes[1] = (byte) j;
				for (int k = 33; k < 127 && !globalStop && !stop; k++) {
					inputBytes[2] = (byte) k;
					for (int l = 33; l < 127 && !globalStop && !stop; l++) {
						inputBytes[3] = (byte) l;
						for (int m = 33; m < 127 && !globalStop && !stop; m++) {
							inputBytes[4] = (byte) m;
							for (int n = 33; n < 127; n++) {
								inputBytes[5] = (byte) n;
								if (md5Matches(6)) {
									foundPassword(new String(inputBytes, 0, 6), startTime);
									return;
								}
								if (++checkCounter >= CHECK_INTERVAL) {
									checkCounter = 0;
									if (globalStop || stop) return;
								}
							}
						}
					}
				}
			}
		}
	}
	
	private void foundPassword(String password, long startTime) {
		System.out.println("Password found : "+password+" by Thread "+id+" on Server "+server);
		double endTime = System.nanoTime();
		double totalTime = endTime - startTime;
		System.out.println("Time : "+(totalTime/1000000000)/60+" minutes");
		Server1.stop_threads();
	}
}