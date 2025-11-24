# Server1.java - Code Explanation

This document explains the key code blocks and their importance in the `Server1.java` server implementation.

---

## Overview

`Server1` is one of two distributed computing nodes that splits the MD5 search workload. It handles the **first half of the ASCII character range** (ASCII 33-79, characters '!' through 'O'). Server1 creates and manages its own pool of worker threads, divides its assigned range among them, and coordinates their startup and shutdown.

---

## 1. Server Identity and Range Assignment

**Lines 14-23**

```java
// List to keep track of all threads created by this server
static List<Search_Thread> threads = new ArrayList<Search_Thread>();

// ASCII range for Server1: ! through O
public static final int START = 33;     // '!' (first printable ASCII)
public static final int END = 80;       // 'P' (exclusive, so up to 'O')
public static final int BASE = 94;      // total characters across both servers
public static final int BASE_OFFSET = 33;
public static final int CHUNK_SIZE = 1024;
public static AtomicLong[] counters = new AtomicLong[7];
public static long[] totals = new long[7];
```

### Why This is Important:

✅ **Server1's Territory - First Half of Character Space**:
- ASCII 33-79 = 47 characters ('!' to 'O')
- Handles all passwords starting with these characters
- Server2 handles ASCII 80-126 (47 characters, 'P' to '~')
- Together they cover all 94 printable ASCII characters

**Character Range Examples:**
```
Server1 handles passwords starting with:
! " # $ % & ' ( ) * + , - . / 0 1 2 3 4 5 6 7 8 9 : ; < = > ? @ A B C D E F G H I J K L M N O

Server2 handles passwords starting with:
P Q R S T U V W X Y Z [ \ ] ^ _ ` a b c d e f g h i j k l m n o p q r s t u v w x y z { | } ~
```

✅ **Thread Management**:
- `threads` list keeps track of all Search_Thread instances
- Allows coordinated starting and stopping
- Static so it persists across method calls

✅ **Work Distribution Constants**:
- `BASE = 94`: Total character set size for arithmetic
- `CHUNK_SIZE = 1024`: How many passwords each thread pulls at once
- `counters[]` and `totals[]`: Same structure as GlobalDistributor but **not actually used in current implementation** (GlobalDistributor handles this)

**Note**: The counters/totals in Server1/Server2 are legacy code and redundant. The actual work distribution uses `GlobalDistributor.counters` and `GlobalDistributor.totals`.

---

## 2. Thread Creation and Starting

**Lines 26-37**

```java
// Create and start all threads for this server
public static void start_threads(String hashcode, List<String> ints) {
    // First create all thread objects
    for(String inte : ints) {
        Search_Thread st = new Search_Thread(inte, hashcode, 1);  // 1 = server ID
        threads.add(st);
    }
    // Then start them all
    for(Search_Thread st : threads) {
        st.start();  // begins execution
    }
}
```

### Why This is Important:

✅ **Two-Phase Initialization**:
1. **Phase 1 (Creation)**: Create all thread objects first
2. **Phase 2 (Starting)**: Start all threads together

**Why Not Create and Start Together?**
```java
// ❌ Could cause timing issues:
for (String inte : ints) {
    Search_Thread st = new Search_Thread(inte, hashcode, 1);
    st.start();  // This thread starts immediately
}
// Problem: First thread gets head start, last thread delayed
```

```java
// ✅ Better - all threads start at roughly same time:
// Create all
for (...) { threads.add(new Search_Thread(...)); }
// Start all
for (...) { st.start(); }
// Result: More fair distribution of work
```

✅ **Server ID = 1**:
- Each thread knows it belongs to Server1 (for reporting)
- Passed to Search_Thread constructor
- Used in output: "Password found by Thread 3 on Server 1"

✅ **Interval Assignment**:
- Each thread gets a string like "33-44" (its assigned first-character range)
- Thread uses this to filter which passwords it tests
- Prevents overlap between threads

---

## 3. Stop Coordination

**Lines 40-45**

```java
// Signal all threads to stop (called when password is found)
public static void stop_threads() {
    for(Search_Thread st : threads) {
        st.setStop(true);  // set stop flag for each thread
    }
}
```

### Why This is Important:

✅ **Centralized Shutdown**:
- Called by SearchCoordinator when password found
- Iterates through all threads and sets their stop flag
- Threads check this flag in their inner loops

✅ **Fast Propagation**:
- `setStop()` sets `volatile boolean stop = true`
- All threads see this within 1-2 milliseconds
- Prevents wasted computation after winner found

✅ **Graceful Termination**:
- Doesn't forcefully kill threads (no Thread.stop())
- Threads check flag and exit cleanly
- Proper resource cleanup

**Call Chain:**
```
SearchCoordinator.reportFound()
    ↓
Server1.stop_threads()  AND  Server2.stop_threads()
    ↓
Each Search_Thread.setStop(true)
    ↓
Threads check stop flag in loops and exit
```

---

## 4. Server Initialization

**Lines 48-66**

```java
// Main entry point to start Server1 with n threads
public static void start_server(String hashcode, int n) {
    System.out.println("Server 1 starting...");
    threads.clear();  // clear any old threads from previous runs
    
    // Calculate search space for this server's range
    int range = END - START;  // how many first chars this server handles
    
    // Pre-calculate totals for each password length
    for (int len = 1; len <= 6; len++) {
        if (len == 1) {
            totals[len] = range;
        } else {
            long suffix = pow(BASE, len - 1);
            totals[len] = range * suffix;
        }
        counters[len] = new AtomicLong(0L);
    }
    
    // Divide work among n threads and start them
    List<String> intervals = intervals(n);
    start_threads(hashcode, intervals);
}
```

### Why This is Important:

✅ **Clean Slate for Each Search**:
- `threads.clear()` removes old threads from previous runs
- Enables multiple consecutive searches without restart
- Prevents memory leaks from accumulating thread references

✅ **Search Space Calculation**:
```
Server1 range = 80 - 33 = 47 characters

For length 3 passwords:
- First char: 47 options (Server1's range)
- Remaining 2 chars: 94² = 8,836 options
- Total for Server1: 47 × 8,836 = 415,292 passwords
```

**Note**: This calculation is redundant since GlobalDistributor also does this. The architecture evolved and these per-server totals/counters are no longer used.

✅ **Thread Pool Creation**:
- Calls `intervals(n)` to divide range into n pieces
- Creates n threads, each with its own sub-range
- Starts all threads simultaneously

**Entry Point:**
```java
// Called by Main_Server:
Server1.start_server(hashcode, threadsServer1);
```

---

## 5. Interval Division Algorithm

**Lines 68-98**

```java
public static List<String> intervals(int n) {
    List<String> inter = new ArrayList<String>();
    int start = START;                  // 33
    int end = END;                      // 80
    int totalRange = end - start;       // 47 characters
    
    // Divide as evenly as possible
    int baseInterval = totalRange / n;           // base size for each thread
    int remainder = totalRange % n;              // some threads get +1
    
    int currentStart = start;
    for (int i = 0; i < n; i++) {
        int intervalSize = baseInterval;
        
        // First 'remainder' threads get an extra character
        if (i < remainder) {
            intervalSize++;
        }
        
        int currentEnd = currentStart + intervalSize;
        
        // Make sure last interval goes exactly to END
        if (i == n - 1) {
            currentEnd = end;
        }
        
        inter.add(currentStart + "-" + currentEnd);
        currentStart = currentEnd;  // next interval starts here
    }
    
    return inter;
}
```

### Why This is Important:

✅ **Even Distribution Algorithm**:
- Divides 47 characters among n threads as evenly as possible
- Handles cases where division isn't perfect (remainder)

**Example: 5 threads on Server1 (47 characters)**
```
47 ÷ 5 = 9 remainder 2

baseInterval = 9
remainder = 2

Thread 0: gets 9 + 1 = 10 chars (33-43)   ← first 2 get extra
Thread 1: gets 9 + 1 = 10 chars (43-53)   ← first 2 get extra
Thread 2: gets 9 chars (53-62)
Thread 3: gets 9 chars (62-71)
Thread 4: gets 9 chars (71-80)

Total: 10 + 10 + 9 + 9 + 9 = 47 ✓
```

✅ **Remainder Distribution**:
```java
if (i < remainder) {
    intervalSize++;  // first 'remainder' threads get +1
}
```
- Distributes extra characters to first threads
- More balanced than giving all extras to one thread
- Prevents one thread from being overloaded

✅ **Interval Format: "start-end"**:
- Example: "33-43" means ASCII 33 through 42 (inclusive start, exclusive end)
- Thread parses this string to know its assigned range
- Displayed in console: `Thread 0 (Server 1) assigned interval 33-43 -> ascii [33..42] ('!'...'*')`

✅ **Edge Case Handling**:
```java
if (i == n - 1) {
    currentEnd = end;  // safety check for last thread
}
```
- Ensures last thread always ends exactly at `END`
- Handles rounding errors or edge cases
- Guarantees full coverage with no gaps

---

## 6. Power Function

**Lines 101-106**

```java
// Helper: calculate base^exp
private static long pow(int base, int exp) {
    long r = 1L;
    for (int i = 0; i < exp; i++) r *= base;
    return r;
}
```

### Why This is Important:

✅ **Same as GlobalDistributor**: Integer-only exponentiation for precision.

✅ **Used in Search Space Calculation**: Computes BASE^(length-1) for totals.

**Note**: Redundant since GlobalDistributor provides the same calculation. This is legacy code from when servers managed their own work distribution.

---

## Server1 vs Server2 Architecture

### Identical Structure, Different Range:

| Feature | Server1 | Server2 |
|---------|---------|---------|
| **ASCII Range** | 33-79 ('!' to 'O') | 80-126 ('P' to '~') |
| **Character Count** | 47 characters | 47 characters |
| **Server ID** | 1 | 2 |
| **Code Structure** | Identical | Identical |
| **Thread Management** | Yes | Yes |
| **Independent Operation** | Yes | Yes |

### Division Strategy:

```
Total ASCII Range: 33-126 (94 chars)
                   ↓
        ┌──────────┴──────────┐
        ▼                     ▼
   Server1 (33-79)       Server2 (80-126)
   47 characters         47 characters
        │                     │
   ┌────┴────┐           ┌────┴────┐
   ▼         ▼           ▼         ▼
Thread1  Thread2    Thread1  Thread2
(split   (split     (split   (split
range)   range)     range)   range)
```

---

## Integration with System

### Server1's Role in the Architecture:

```
┌────────────────────────────────────────────────┐
│ Main_Server                                    │
│ • Splits threads: Server1 gets ceil(n/2)      │
│ • Calls Server1.start_server(hash, threads)   │
└─────────────────┬──────────────────────────────┘
                  │
                  ▼
      ┌──────────────────────┐
      │ Server1              │
      │ • Range: 33-79       │
      │ • Creates threads    │
      │ • Divides its range  │
      └─────────┬────────────┘
                │
    ┌───────────┼───────────┐
    ▼           ▼           ▼
[Thread0]   [Thread1]   [Thread2]
(33-43)     (43-53)     (53-62)...
    │           │           │
    └───────────┼───────────┘
                ▼
    ┌────────────────────────┐
    │ GlobalDistributor      │
    │ • Provides chunks      │
    │ • All threads pull     │
    └────────────────────────┘
```

### Call Sequence:

**1. Main_Server starts Server1:**
```java
int threadsServer1 = (totalThreads + 1) / 2;  // e.g., 5 threads if total=10
Server1.start_server(hashcode, threadsServer1);
```

**2. Server1 divides its range:**
```java
List<String> intervals = intervals(5);
// Returns: ["33-43", "43-53", "53-62", "62-71", "71-80"]
```

**3. Server1 creates threads:**
```java
for (String interval : intervals) {
    Search_Thread st = new Search_Thread(interval, hashcode, 1);
    threads.add(st);
}
```

**4. Server1 starts threads:**
```java
for (Search_Thread st : threads) {
    st.start();  // Thread begins execution
}
```

**5. Threads run independently:**
- Each thread processes its interval
- All threads pull chunks from GlobalDistributor
- First thread to find password reports to SearchCoordinator

**6. SearchCoordinator stops all:**
```java
Server1.stop_threads();  // Sets stop flag on all Server1 threads
Server2.stop_threads();  // Sets stop flag on all Server2 threads
```

---

## Why Two Servers?

### Design Rationale:

✅ **Logical Distribution**:
- Demonstrates distributed systems concept
- Each server is independent unit
- Could run on separate machines (with minor modifications)

✅ **Load Balancing**:
- Split 94 characters into two 47-char ranges
- Each server handles ~50% of search space
- Prevents single point of bottleneck

✅ **Scalability Demonstration**:
- Architecture supports extending to more servers
- Server3, Server4, etc. could handle 1/4 each
- Easy to add horizontal scaling

✅ **Fault Tolerance Ready**:
- If one server crashes, other continues
- (Current implementation doesn't handle crashes, but architecture supports it)

### Current Limitation:

**Both servers run in same JVM (single process):**
- Share memory space
- Access same GlobalDistributor
- Not truly distributed across machines

**To Make Truly Distributed:**
1. Run Server1 and Server2 in separate JVM processes/machines
2. Replace GlobalDistributor with network-based coordinator (Redis, RabbitMQ, etc.)
3. Replace SearchCoordinator with distributed consensus (Zookeeper, etcd, etc.)

---

## Performance Characteristics

### Thread Creation Overhead:

```
Creating 5 threads: ~1-5 milliseconds
Starting 5 threads: ~1-5 milliseconds
Total setup time: ~10 milliseconds
```
**Negligible** compared to search time (seconds to hours).

### Interval Division Complexity:

```
Time: O(n) where n = number of threads
Space: O(n) for storing interval strings
```
**Extremely fast** even for large n (e.g., n=100 would still be instant).

### Thread Coordination:

```
stop_threads(): O(n) iteration to set flags
Propagation time: 1-2 milliseconds across all threads
```
**Very efficient** - no locks, just flag setting.

---

## Common Questions

**Q: Why not just use one server with all threads?**  
A: Two servers demonstrate distributed architecture. In production, they'd run on separate machines for true distribution.

**Q: Why does Server1 get ceil(n/2) threads instead of floor(n/2)?**  
A: For odd thread counts (e.g., 7), Server1 gets 4 and Server2 gets 3. This is arbitrary but fair.

**Q: What if threads finish at different times?**  
A: That's fine! GlobalDistributor's chunk-based system handles this. Fast threads pull more chunks, slow threads pull fewer.

**Q: Why create all threads before starting any?**  
A: Ensures fair start. If we created and started one-by-one, first threads would have head start.

**Q: Can we have 3 or more servers?**  
A: Architecture supports it! Would need Server3, Server4, etc., each handling a portion of ASCII range.

**Q: Why are counters/totals in Server1 when GlobalDistributor has them?**  
A: Legacy code from earlier design. Architecture evolved to use GlobalDistributor for central coordination. Server-level counters are no longer used.

---

## Code Example: Thread Division

### Scenario: Start Server1 with 4 threads

**Input:**
```java
Server1.start_server("5f4dcc3b5aa765d61d8327deb882cf99", 4);
```

**Execution:**
```java
// Step 1: Clear old threads
threads.clear();

// Step 2: Calculate intervals
range = 80 - 33 = 47
baseInterval = 47 / 4 = 11
remainder = 47 % 4 = 3

// Step 3: Distribute
Thread 0: 11 + 1 = 12 chars  →  "33-45"
Thread 1: 11 + 1 = 12 chars  →  "45-57"
Thread 2: 11 + 1 = 12 chars  →  "57-69"
Thread 3: 11 chars           →  "69-80"

Total: 12 + 12 + 12 + 11 = 47 ✓

// Step 4: Create threads
for each interval: new Search_Thread(interval, hash, 1)

// Step 5: Start threads
for each thread: st.start()
```

**Console Output:**
```
Server 1 starting...
Thread 0 (Server 1) assigned interval 33-45 -> ascii [33..44] ('!'...',')
Thread 1 (Server 1) assigned interval 45-57 -> ascii [45..56] ('-'...'8')
Thread 2 (Server 1) assigned interval 57-69 -> ascii [57..68] ('9'...'D')
Thread 3 (Server 1) assigned interval 69-80 -> ascii [69..79] ('E'...'O')
```

---

## Key Takeaways for Presentation

1. **First-Level Distribution** - Server1 handles first half of ASCII range (33-79), demonstrating coarse-grained partitioning

2. **Independent Thread Management** - Server creates, starts, and stops its own thread pool autonomously

3. **Even Interval Division** - Smart algorithm distributes remainder characters fairly among threads

4. **Coordinated Shutdown** - Centralized stop_threads() method enables immediate termination across all threads

5. **Scalable Architecture** - Design supports extending to 3+ servers or running on separate machines

6. **Two-Phase Initialization** - Create all threads first, then start together for fair work distribution

7. **Logical Distribution Layer** - Demonstrates distributed systems concept even though current implementation runs in single JVM

---

*Server1 demonstrates the distributed computing node pattern: independent operation, local resource management, coordinated startup/shutdown, and clear responsibility boundaries in a multi-node system.*
