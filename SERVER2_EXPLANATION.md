# Server2.java - Code Explanation

This document explains the key code blocks and their importance in the `Server2.java` server implementation.

---

## Overview

`Server2` is the second distributed computing node that handles the **second half of the ASCII character range** (ASCII 80-126, characters 'P' through '~'). Server2 mirrors Server1's structure but operates on a different range, completing the distribution of the full 94-character search space.

---

## 1. Server Identity and Range Assignment

**Lines 13-23**

```java
// Keep track of all threads for this server
static List<Search_Thread> threads = new ArrayList<Search_Thread>();

// Server2's ASCII range: P through ~ (tilde)
public static final int START = 80;     // 'P'
public static final int END = 127;      // DEL (exclusive, so up to '~')
public static final int BASE = 94;      // same base for calculation purposes
public static final int BASE_OFFSET = 33;
public static final int CHUNK_SIZE = 1024;
public static AtomicLong[] counters = new AtomicLong[7];
public static long[] totals = new long[7];
```

### Why This is Important:

✅ **Server2's Territory - Second Half of Character Space**:
- ASCII 80-126 = 47 characters ('P' to '~')
- Handles all passwords starting with these characters
- Complements Server1 (ASCII 33-79)
- Together: 47 + 47 = 94 total characters covered

**Character Range Examples:**
```
Server1 handles passwords starting with (ASCII 33-79):
! " # $ % & ' ( ) * + , - . / 0 1 2 3 4 5 6 7 8 9 : ; < = > ? @ A B C D E F G H I J K L M N O

Server2 handles passwords starting with (ASCII 80-126):
P Q R S T U V W X Y Z [ \ ] ^ _ ` a b c d e f g h i j k l m n o p q r s t u v w x y z { | } ~
```

✅ **Symmetric Design**:
- Same constants as Server1 (BASE, CHUNK_SIZE, BASE_OFFSET)
- Same thread management structure
- Only difference: ASCII range (START, END)

✅ **Server ID = 2**:
- Passed to Search_Thread constructor
- Used in reporting: "Password found by Thread 5 on Server 2"
- Distinguishes threads from Server1 vs Server2

---

## 2. Thread Creation and Starting

**Lines 26-37**

```java
// Initialize and launch all threads
public static void start_threads(String hashcode, List<String> ints) {
    // Create thread objects first
    for(String inte : ints) {
        Search_Thread st = new Search_Thread(inte, hashcode, 2);  // 2 = Server2 ID
        threads.add(st);
    }
    // Start all threads
    for(Search_Thread st : threads) {
        st.start();
    }
}
```

### Why This is Important:

✅ **Identical Pattern to Server1**:
- Two-phase initialization (create then start)
- Ensures all threads start at roughly same time
- Fair work distribution from the beginning

✅ **Server ID = 2**:
- Only difference from Server1 (which uses ID = 1)
- Allows tracking which server found the password
- Useful for performance analysis and debugging

---

## 3. Stop Coordination

**Lines 40-45**

```java
// Tell all threads to stop searching
public static void stop_threads() {
    for(Search_Thread st : threads) {
        st.setStop(true);  // sets the stop flag
    }
}
```

### Why This is Important:

✅ **Same Graceful Shutdown as Server1**:
- Called by SearchCoordinator when password found
- Sets volatile stop flag on each thread
- Threads check flag and exit cleanly

✅ **Cross-Server Coordination**:
```java
// In SearchCoordinator.reportFound():
Server1.stop_threads();  // Stops all Server1 threads
Server2.stop_threads();  // Stops all Server2 threads
// Both called regardless of which server found the password
```

---

## 4. Server Initialization

**Lines 48-66**

```java
// Start Server2 with the specified number of threads
public static void start_server(String hashcode, int n) {
    threads.clear();  // remove old threads
    
    // Calculate how many passwords this server needs to check
    int range = END - START;  // 127 - 80 = 47 characters
    
    // Setup totals and counters for each length
    for (int len = 1; len <= 6; len++) {
        if (len == 1) {
            totals[len] = range;
        } else {
            long suffix = pow(BASE, len - 1);
            totals[len] = range * suffix;
        }
        counters[len] = new AtomicLong(0L);
    }
    
    // Calculate intervals and start threads
    List<String> intervals = intervals(n);
    start_threads(hashcode, intervals);
}
```

### Why This is Important:

✅ **Identical Logic to Server1**:
- Clear old threads for new search
- Calculate search space for Server2's range
- Divide range and create threads

✅ **Server2's Search Space**:
```
Server2 range = 127 - 80 = 47 characters

For length 3 passwords:
- First char: 47 options (Server2's range)
- Remaining 2 chars: 94² = 8,836 options
- Total for Server2: 47 × 8,836 = 415,292 passwords

Combined with Server1: 415,292 + 415,292 = 830,584 total 3-char passwords ✓
```

✅ **Entry Point**:
```java
// Called by Main_Server:
int threadsServer2 = totalThreads - threadsServer1;
Server2.start_server(hashcode, threadsServer2);
```

---

## 5. Interval Division Algorithm

**Lines 69-98**

```java
// Divide the range into n intervals (one per thread)
public static List<String> intervals(int n) {
    List<String> inter = new ArrayList<String>();
    int start = START;                  // 80
    int end = END;                      // 127
    int totalRange = end - start;       // 47 characters
    
    // Split range as evenly as possible
    int baseInterval = totalRange / n;
    int remainder = totalRange % n;
    
    int currentStart = start;
    for (int i = 0; i < n; i++) {
        int intervalSize = baseInterval;
        
        // Give extra character to first 'remainder' threads
        if (i < remainder) {
            intervalSize++;
        }
        
        int currentEnd = currentStart + intervalSize;
        
        // Ensure last thread ends exactly at END
        if (i == n - 1) {
            currentEnd = end;
        }
        
        inter.add(currentStart + "-" + currentEnd);
        currentStart = currentEnd;
    }
    
    return inter;
}
```

### Why This is Important:

✅ **Same Algorithm as Server1, Different Range**:
- Divides 47 characters (80-126) among n threads
- Fair distribution with remainder handling
- Guarantees full coverage with no gaps or overlaps

**Example: 5 threads on Server2 (47 characters)**
```
47 ÷ 5 = 9 remainder 2

Thread 0: gets 9 + 1 = 10 chars (80-90)    → 'P' to 'Y'
Thread 1: gets 9 + 1 = 10 chars (90-100)   → 'Z' to 'c'
Thread 2: gets 9 chars (100-109)           → 'd' to 'l'
Thread 3: gets 9 chars (109-118)           → 'm' to 'u'
Thread 4: gets 9 chars (118-127)           → 'v' to '~'

Total: 10 + 10 + 9 + 9 + 9 = 47 ✓
```

✅ **Interval Format Examples**:
```
Server2 intervals for 3 threads:
- "80-96"  → Thread 0 handles 'P' through '_' (16 chars)
- "96-112" → Thread 1 handles '`' through 'o' (16 chars)
- "112-127" → Thread 2 handles 'p' through '~' (15 chars)
```

---

## 6. Power Function

**Lines 101-106**

```java
// Power function (same as Server1)
private static long pow(int base, int exp) {
    long r = 1L;
    for (int i = 0; i < exp; i++) r *= base;
    return r;
}
```

### Why This is Important:

✅ **Identical to Server1**: Integer exponentiation for search space calculation.

---

## Server1 and Server2 Comparison

### Side-by-Side Comparison:

| Feature | Server1 | Server2 |
|---------|---------|---------|
| **ASCII Start** | 33 ('!') | 80 ('P') |
| **ASCII End** | 80 ('P', exclusive) | 127 (DEL, exclusive) |
| **Characters Handled** | 47 | 47 |
| **Example First Chars** | !, @, A, O | P, Z, a, ~ |
| **Server ID** | 1 | 2 |
| **Code Structure** | Identical | Identical |
| **Thread Management** | Yes | Yes |
| **Interval Division** | Yes | Yes |
| **Search Space (len=3)** | 415,292 | 415,292 |
| **Called By** | Main_Server | Main_Server |

### Perfect Symmetry:

```
Total ASCII Range: 33-126 (94 printable characters)
                   ↓
        ┌──────────┴──────────┐
        ▼                     ▼
Server1 (33-79)           Server2 (80-126)
47 chars                  47 chars
'!' through 'O'           'P' through '~'
```

---

## Integration with Main_Server

### Thread Distribution Logic:

```java
// In Main_Server.java:
int totalThreads = 10;  // user input

// Split threads between servers
int threadsServer1 = (totalThreads + 1) / 2;     // ceil(10/2) = 5
int threadsServer2 = totalThreads - threadsServer1;  // 10 - 5 = 5

Server1.start_server(hashcode, 5);  // Server1 gets 5 threads
Server2.start_server(hashcode, 5);  // Server2 gets 5 threads
```

**For Odd Thread Counts:**
```java
totalThreads = 7;
threadsServer1 = (7 + 1) / 2 = 4
threadsServer2 = 7 - 4 = 3

// Server1 gets 4 threads, Server2 gets 3 threads
```

### Startup Sequence:

```
1. Main_Server
   ↓
2. SearchCoordinator.reset()
3. GlobalDistributor.init()
   ↓
4. Server1.start_server(hash, n1)
   ├─→ Create n1 threads
   └─→ Start all threads
   ↓
5. Server2.start_server(hash, n2)
   ├─→ Create n2 threads
   └─→ Start all threads
   ↓
6. All threads run independently
   ↓
7. First to find password:
   SearchCoordinator.reportFound()
   ↓
8. SearchCoordinator stops all:
   Server1.stop_threads()
   Server2.stop_threads()
```

---

## Why Two Servers Instead of One?

### Advantages of Two-Server Design:

✅ **Demonstrates Distribution Concept**:
- Shows how work can be partitioned across nodes
- Foundation for true multi-machine deployment
- Educational value for distributed systems

✅ **Logical Separation**:
- Each server has clear responsibility (first-char range)
- Independent operation
- No inter-server communication needed during search

✅ **Balanced Load**:
- 47 + 47 = 94 characters split evenly
- Each server handles ~50% of search space
- Prevents single point of bottleneck

✅ **Scalability Ready**:
- Easy to extend to Server3, Server4, etc.
- Each additional server handles smaller range
- Horizontal scaling pattern

✅ **Fault Tolerance Potential**:
- If running on separate machines, one failure doesn't stop other
- (Current implementation runs both in same JVM, but architecture supports separation)

### Path to True Distribution:

**Current State (Pseudo-Distributed):**
```
Single Machine
├─ Single JVM Process
│  ├─ Server1 (threads)
│  ├─ Server2 (threads)
│  ├─ GlobalDistributor (shared memory)
│  └─ SearchCoordinator (shared memory)
```

**True Distributed (with modifications):**
```
Machine A                  Machine B
├─ JVM Process 1           ├─ JVM Process 2
│  └─ Server1              │  └─ Server2
│                          │
└─→ Network ←──────────────┘
    ↓
Shared Coordinator Service (Redis/RabbitMQ/Kafka)
├─ Work Queue (replaces GlobalDistributor)
└─ Result Coordinator (replaces SearchCoordinator)
```

---

## Performance Characteristics

### Identical to Server1:

- **Thread Creation**: ~1-5 ms for typical thread counts
- **Interval Division**: O(n), instant for reasonable n
- **Stop Propagation**: 1-2 ms across all threads
- **Memory Usage**: ~minimal per server (~1 KB overhead)

### Combined System Performance:

```
With 10 total threads (5 per server):
- Server1: 5 threads × 47 chars = 235 char-threads
- Server2: 5 threads × 47 chars = 235 char-threads
- Total: 10 threads covering all 94 characters

Speedup: ~10x compared to single-threaded (ideal case)
```

---

## Common Questions

**Q: Why does Server2 start at ASCII 80 specifically?**  
A: That's where Server1 ends (END=80). Server2.START = Server1.END ensures no gap or overlap.

**Q: What if we want 3 servers?**  
A: Divide 94 chars by 3:
- Server1: 33-64 (31 chars)
- Server2: 64-95 (31 chars)  
- Server3: 95-127 (32 chars)

**Q: Is Server2 slower than Server1 because it handles different characters?**  
A: No. Both handle 47 characters. Password distribution across ranges is roughly even.

**Q: Can Server1 and Server2 run at different speeds?**  
A: Yes, but GlobalDistributor's dynamic chunking compensates. Faster server pulls more chunks automatically.

**Q: What if Server2's threads find the password before Server1's threads start?**  
A: That's fine! SearchCoordinator handles winner selection atomically. Server1 threads will stop as soon as they start.

**Q: Why not put both servers in the same class?**  
A: Separate classes demonstrate modularity and make it easier to deploy on separate machines in the future.

---

## Code Example: Complete Server2 Flow

### Scenario: 3 threads on Server2

```java
// 1. Called by Main_Server
Server2.start_server("5f4dcc3b5aa765d61d8327deb882cf99", 3);

// 2. Inside start_server()
threads.clear();
range = 127 - 80 = 47

// 3. intervals(3)
baseInterval = 47 / 3 = 15
remainder = 47 % 3 = 2

Thread 0: 15 + 1 = 16 chars → "80-96"   ('P' to '_')
Thread 1: 15 + 1 = 16 chars → "96-112"  ('`' to 'o')
Thread 2: 15 chars → "112-127"          ('p' to '~')

// 4. start_threads()
for each interval:
    new Search_Thread(interval, hash, 2)  // server ID = 2

// 5. Start all
for each thread: st.start()

// 6. Console output:
Thread 0 (Server 2) assigned interval 80-96 -> ascii [80..95] ('P'...'_')
Thread 1 (Server 2) assigned interval 96-112 -> ascii [96..111] ('`'...'o')
Thread 2 (Server 2) assigned interval 112-127 -> ascii [112..126] ('p'...'~')

// 7. Threads run and search
// 8. If Thread 1 finds password:
SearchCoordinator.reportFound("password", 1, 2)
// Output: "Password found : password by Thread 1 on Server 2"

// 9. All threads stopped
Server1.stop_threads()
Server2.stop_threads()
```

---

## Key Takeaways for Presentation

1. **Second Distribution Node** - Server2 completes the two-server distribution by handling ASCII 80-126 ('P' to '~')

2. **Perfect Symmetry** - Mirrors Server1's structure exactly, only difference is ASCII range

3. **Complementary Coverage** - Server1 + Server2 = complete 94-character search space with no gaps or overlaps

4. **Independent Operation** - Operates autonomously with its own thread pool and interval division

5. **Identical Stop Coordination** - Both servers stopped simultaneously when password found, regardless of which server found it

6. **Balanced Workload** - 47 characters each ensures fair distribution of search space

7. **Scalable Foundation** - Design pattern can extend to 3+ servers for even finer-grained distribution

---

*Server2 completes the distributed architecture, demonstrating how multiple independent nodes can work in parallel on complementary portions of a problem space, coordinating only at start and finish for maximum efficiency.*
