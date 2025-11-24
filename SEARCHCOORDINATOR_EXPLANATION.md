# SearchCoordinator.java - Code Explanation

This document explains the key code blocks and their importance in the `SearchCoordinator.java` coordination manager.

---

## Overview

`SearchCoordinator` is the central coordinator that manages the global search state. It tracks whether a password has been found, measures timing, and ensures only one thread is declared the winner even if multiple threads find the password simultaneously. This class is critical for atomic winner selection and coordinated stopping across all threads.

---

## 1. Atomic Boolean and Volatile State Variables

**Lines 10-15**

```java
// Using AtomicBoolean for thread-safe flag
private static final AtomicBoolean found = new AtomicBoolean(false);
private static volatile long startTimeNano = 0L;      // when search started
private static volatile String foundPassword = null;   // the password that was found
private static volatile int foundThreadId = -1;        // which thread found it
private static volatile int foundServer = -1;          // which server found it
```

### Why This is Important:

✅ **AtomicBoolean found - The Critical Flag**:
- Thread-safe without locks
- Enables atomic `compareAndSet()` operation (test-and-set in one atomic step)
- All threads check this flag: `if (!found.get()) continue searching`
- Single source of truth for "has password been found?"

✅ **volatile Keyword - Memory Visibility**:
- Ensures changes made by one thread are immediately visible to all other threads
- Without `volatile`, CPU caching could cause threads to see stale values
- Critical in multi-core systems where each core has its own cache

✅ **State Variables**:
- `startTimeNano`: Timestamp when search began (for elapsed time calculation)
- `foundPassword`: The actual password string that matched the MD5 hash
- `foundThreadId`: ID of the winning thread (for reporting)
- `foundServer`: Which server (1 or 2) the winner belongs to

**Why volatile for these but AtomicBoolean for found?**
- These variables are only written ONCE by the winner (no concurrent writes)
- `found` requires atomic test-and-set operation (multiple threads racing)
- `volatile` ensures visibility, `AtomicBoolean` ensures atomicity

---

## 2. Reset Method

**Lines 17-23**

```java
public static void reset() {
    found.set(false);
    startTimeNano = 0L;
    foundPassword = null;
    foundThreadId = -1;
    foundServer = -1;
}
```

### Why This is Important:

✅ **Enables Multiple Consecutive Searches**:
- User can test multiple MD5 hashes without restarting program
- Resets all state to initial values
- Called by Main_Server before each new search

✅ **Clean State Guarantee**:
- Ensures no leftover data from previous search
- All threads start with `found = false`
- Winner info cleared

**Usage Flow:**
```
1. First search: init() → search → found password
2. User wants to try another hash
3. reset() called → all state cleared
4. New search starts fresh
```

---

## 3. Getter Methods

**Lines 25-45**

```java
public static void setStartTime(long t) {
    startTimeNano = t;
}

public static long getStartTime() {
    return startTimeNano;
}

public static boolean isFound() {
    return found.get();
}

public static String getFoundPassword() {
    return foundPassword;
}

public static int getFoundThreadId() {
    return foundThreadId;
}

public static int getFoundServer() {
    return foundServer;
}
```

### Why This is Important:

✅ **isFound() - The Most Called Method**:
- Called by EVERY thread in EVERY loop iteration
- Returns current state of `found` flag
- Threads stop when this returns `true`
- Called millions of times per second across all threads

✅ **Thread-Safe Read Access**:
- `AtomicBoolean.get()` is thread-safe
- `volatile` variables provide safe reads
- No locks needed for reading

✅ **Encapsulation**:
- Private fields with public getters
- Controlled access to internal state
- Prevents accidental modification

**Performance Consideration:**
- `isFound()` must be extremely fast (no locks, just memory read)
- Called so frequently that even nanoseconds matter
- Atomic reads are optimized at CPU level

---

## 4. Winner Selection - The Most Critical Method

**Lines 47-64**

```java
// Called by thread when it finds the password
public static void reportFound(String password, int threadId, int server) {
    // Use compareAndSet to ensure only first thread reports
    // This is atomic - only one thread will get true back
    if (found.compareAndSet(false, true)) {
        foundPassword = password;
        foundThreadId = threadId;
        foundServer = server;
        long end = System.nanoTime();
        double totalSec = (end - startTimeNano) / 1_000_000_000.0;
        System.out.println("Password found : " + password + " by Thread " + threadId + " on Server " + server);
        System.out.printf("Time : %.6f seconds (%.6f minutes)%n", totalSec, totalSec / 60.0);
        
        Server1.stop_threads();
        Server2.stop_threads();
    }
}
```

### Why This is Important:

✅ **compareAndSet(false, true) - Atomic Winner Selection**:
```java
// What happens internally:
if (current_value == false) {
    set_value_to_true();
    return true;  // YOU ARE THE WINNER
} else {
    return false; // Someone else already won
}
// ALL OF THIS HAPPENS IN ONE ATOMIC OPERATION
```

✅ **Race Condition Prevention**:
- Multiple threads might find the password at nearly the same time
- `compareAndSet()` guarantees only ONE returns `true`
- Only the winner enters the `if` block
- Losers get `false` and their report is ignored

**Scenario Without compareAndSet (WRONG):**
```java
// ❌ BROKEN CODE - Race condition!
if (!found.get()) {           // Thread A checks: false
    found.set(true);          // Thread A sets to true
                              // Thread B checks before A's set completes!
    System.out.println(...);  // BOTH threads print!
}
```

**With compareAndSet (CORRECT):**
```java
// ✅ CORRECT - Atomic operation
if (found.compareAndSet(false, true)) {
    // Only ONE thread ever enters here
    System.out.println(...);
}
```

✅ **Winner Records Results**:
- Only the winning thread stores `foundPassword`, `foundThreadId`, `foundServer`
- Other threads that found the password are ignored (their results discarded)
- First to report wins

✅ **Elapsed Time Calculation**:
```java
long end = System.nanoTime();
double totalSec = (end - startTimeNano) / 1_000_000_000.0;
```
- Uses nanosecond precision timestamps
- Converts to seconds for readability
- Also displays minutes for long searches

✅ **Global Stop Signal**:
```java
Server1.stop_threads();
Server2.stop_threads();
```
- Winner immediately signals ALL threads to stop
- Prevents wasted computation
- Both servers notified regardless of which found it

---

## Atomic Operations Deep Dive

### What Makes compareAndSet "Atomic"?

**CPU-Level Guarantee:**
```
Modern CPUs provide compare-and-swap (CAS) instructions:
- x86: CMPXCHG instruction
- ARM: LDREX/STREX pair
- Guaranteed atomic at hardware level
- Cannot be interrupted mid-operation
```

**Visual Timeline:**

**Scenario: Thread A and Thread B both find password at nanosecond 1000**

```
Time (ns)   Thread A                    Thread B
────────────────────────────────────────────────────────
1000        Calls reportFound()         Calls reportFound()
1001        compareAndSet(false,true)   compareAndSet(false,true)
1002        ↓ Returns: TRUE ✓           ↓ Returns: FALSE ✗
1003        Enters if block             Skips if block
1004        Stores password             (Does nothing)
1005        Prints result               (Does nothing)
1006        Calls stop_threads()        (Does nothing)
```

**Key Point**: Even though both called at "same time", CPU serializes the atomic operations. One goes first (arbitrary which), the other sees `found` is already `true`.

---

## Memory Visibility and Happens-Before Relationships

### Why volatile Matters:

**Without volatile:**
```
CPU Core 1 (Thread 1)              CPU Core 2 (Thread 2)
─────────────────────              ─────────────────────
found = true (in cache)            Reads found: false (stale!)
                                   Continues searching (wasted work)
```

**With volatile:**
```
CPU Core 1 (Thread 1)              CPU Core 2 (Thread 2)
─────────────────────              ─────────────────────
found = true                       Memory barrier ensures
  ↓ Flush to main memory            ↓ fresh read from main memory
  ↓                                Reads found: true
  ↓                                Stops immediately ✓
```

### Java Memory Model Guarantees:

✅ **volatile writes** are visible to all subsequent reads  
✅ **AtomicBoolean** operations establish happens-before relationships  
✅ **No reordering** of volatile variable accesses  

---

## Integration with Other Components

### How SearchCoordinator Connects:

```
┌──────────────────────────────────────────────────────┐
│ Main_Server                                          │
│ • Calls reset() before search                        │
│ • Calls setStartTime(System.nanoTime())              │
└─────────────────┬────────────────────────────────────┘
                  │
     ┌────────────┴────────────┐
     ▼                         ▼
┌─────────────┐          ┌─────────────┐
│ Server1     │          │ Server2     │
│ • Threads   │          │ • Threads   │
└──────┬──────┘          └──────┬──────┘
       │                        │
       └────────┬───────────────┘
                ▼
    ┌────────────────────────┐
    │ Search_Thread (all)    │
    │ • Check isFound()      │◄──┐
    │   every iteration      │   │ Fast read
    │                        │   │ (no locks)
    │ • Call reportFound()   │   │
    │   when match found     │───┘
    └────────┬───────────────┘
             │
             ▼
  ┌─────────────────────────────┐
  │ SearchCoordinator           │
  │ • compareAndSet() chooses   │
  │   ONE winner atomically     │
  │ • Records results           │
  │ • Triggers global stop      │
  └─────────────────────────────┘
```

### Call Frequency:

| Method | Called By | Frequency |
|--------|----------|-----------|
| `reset()` | Main_Server | Once per search |
| `setStartTime()` | Main_Server | Once per search |
| `isFound()` | All threads | Millions/sec |
| `reportFound()` | Winner thread | Once per search |
| `getFoundPassword()` | Main_Server | Once at end |

---

## Performance Analysis

### Method Performance:

```java
// EXTREMELY FAST (no synchronization needed)
public static boolean isFound() {
    return found.get();  // ~1-2 nanoseconds
}

// FAST (single atomic operation)
public static void reportFound(...) {
    if (found.compareAndSet(false, true)) {  // ~10-20 nanoseconds
        // ... (only one thread executes this)
    }
}
```

### Why This Design is Optimal:

✅ **Hot Path is Lock-Free**:
- `isFound()` called millions of times (no locks = fast)
- Atomic reads are as fast as regular reads on modern CPUs

✅ **Winner Selection is One Atomic Op**:
- `compareAndSet()` is single CPU instruction
- Faster than any lock-based approach

✅ **Stop Propagation is Immediate**:
- `volatile found = true` visible to all cores within nanoseconds
- Threads check `isFound()` in inner loops
- Typical stop time: 1-2 milliseconds across all threads

### Performance Comparison:

| Approach | Winner Selection Time | Lock Contention | Complexity |
|----------|---------------------|-----------------|------------|
| **This (AtomicBoolean)** | ~20 ns | None | Low |
| Synchronized block | ~100-1000 ns | High | Medium |
| ReentrantLock | ~50-500 ns | Medium | High |
| Semaphore | ~100-2000 ns | High | High |

---

## Edge Cases and Thread Safety

### Scenario 1: Multiple Winners at Exact Same Time

```java
// Thread A, B, C all find password at nanosecond 5000
Thread A: compareAndSet(false, true) → TRUE  ✓ (winner)
Thread B: compareAndSet(false, true) → FALSE ✗ (loser)
Thread C: compareAndSet(false, true) → FALSE ✗ (loser)

Result: Thread A's password recorded, others ignored
```

### Scenario 2: Thread Finds Password But Gets Delayed

```java
Thread 1: Finds password at time 1000
Thread 2: Finds password at time 1500
Thread 1: [Context switch delay]
Thread 2: Calls reportFound() first → WINNER
Thread 1: Calls reportFound() → FALSE (too late)

Result: Thread 2 wins even though Thread 1 found it first
```
**Note**: This is acceptable - we only care that ONE winner is chosen, not necessarily the absolute first.

### Scenario 3: No Password Found

```java
All threads exhaust search space
found remains false
Main_Server joins all threads
No output from SearchCoordinator
Main_Server prints "Search finished" (no password found)
```

---

## Why Static Methods and Fields?

### Design Rationale:

✅ **Global Singleton Pattern**:
- Only one search state exists at any time
- All threads across both servers access the same instance
- No need to pass coordinator reference around

✅ **Simplicity**:
- No object creation needed
- Direct access: `SearchCoordinator.isFound()`
- Clear and concise

✅ **Thread Safety Built-In**:
- `static` doesn't mean thread-unsafe
- `AtomicBoolean` and `volatile` provide safety
- No instance state to corrupt

**Alternative (more complex, same result):**
```java
// ❌ More verbose, same functionality
SearchCoordinator coord = new SearchCoordinator();
Server1.start(coord);
Server2.start(coord);
// Each thread needs reference to coord
```

**Current design (simpler, cleaner):**
```java
// ✅ Simple and clear
SearchCoordinator.reset();
// All threads just call SearchCoordinator.isFound()
```

---

## Timing Precision

### Why System.nanoTime()?

```java
long start = System.nanoTime();
// ... search happens ...
long end = System.nanoTime();
double seconds = (end - start) / 1_000_000_000.0;
```

✅ **Nanosecond Precision**:
- Much more accurate than `System.currentTimeMillis()` (millisecond precision)
- Can measure very fast searches (< 1 second) accurately

✅ **Monotonic Clock**:
- Not affected by system clock adjustments
- Always increases (never goes backward)
- Reliable for measuring elapsed time

**Example Output:**
```
Time : 42.196734 seconds (0.703279 minutes)
```
6 decimal places show precision down to microseconds.

---

## Code Examples

### Complete Flow Example:

**1. Initialization (Main_Server):**
```java
SearchCoordinator.reset();
GlobalDistributor.init();
SearchCoordinator.setStartTime(System.nanoTime());
Server1.start_server(hashcode, threadsServer1);
Server2.start_server(hashcode, threadsServer2);
```

**2. During Search (All Threads):**
```java
while (!SearchCoordinator.isFound() && !stop) {
    // Pull chunk, test passwords
    if (md5Matches(length)) {
        SearchCoordinator.reportFound(password, id, server);
        return;
    }
}
```

**3. Winner Declared:**
```java
// Inside reportFound() - only winner executes this:
if (found.compareAndSet(false, true)) {
    foundPassword = password;
    foundThreadId = threadId;
    foundServer = server;
    System.out.println("Password found : " + password + "...");
    Server1.stop_threads();
    Server2.stop_threads();
}
```

**4. After Search (Main_Server):**
```java
// Wait for all threads
for (Thread t : allThreads) t.join();

// Check result
if (SearchCoordinator.isFound()) {
    String pwd = SearchCoordinator.getFoundPassword();
    // Display results
}
```

---

## Common Questions

**Q: What if multiple threads find different passwords with same MD5 hash?**  
A: MD5 collisions are theoretically possible, but for printable 2-6 char passwords, extremely rare. If it happened, first to call `reportFound()` wins.

**Q: Why not use synchronized instead of AtomicBoolean?**  
A: `synchronized` requires locking, which:
- Adds overhead (~10-100x slower)
- Creates contention (threads wait)
- Can deadlock if not careful
`AtomicBoolean` is lock-free and faster.

**Q: Can a thread see stale value of `found` even with volatile?**  
A: No. `volatile` guarantees happens-before relationship. A write to volatile variable is visible to all subsequent reads on any thread.

**Q: What if no thread calls reportFound() (no password found)?**  
A: `found` remains `false`, all threads exhaust work and exit normally, Main_Server prints "Search finished" with no password output.

**Q: Why record both threadId and server?**  
A: For debugging and statistics. Shows distribution of work (which server finds passwords more often).

**Q: Could we use CountDownLatch or CyclicBarrier instead?**  
A: Those are for coordination/synchronization barriers, not winner selection. We need atomic test-and-set, which `compareAndSet()` provides perfectly.

---

## Key Design Patterns

### 1. Test-and-Set (compareAndSet)
```java
if (found.compareAndSet(false, true)) {
    // Critical section - only one thread ever enters
}
```
Classic synchronization primitive, implemented atomically at CPU level.

### 2. Memory Barrier (volatile)
```java
private static volatile String foundPassword;
// Ensures writes visible across all CPU cores
```
Prevents CPU cache coherency issues in multi-core systems.

### 3. Happens-Before Relationship
```java
Thread A: found.set(true);         // Happens-before
Thread B: if (found.get()) {...}   // Sees true
```
Java Memory Model guarantees ordering of volatile operations.

---

## Summary Table

| Feature | Implementation | Benefit |
|---------|---------------|---------|
| **Winner Selection** | `compareAndSet(false, true)` | Atomic, one winner guaranteed |
| **Stop Signal** | `AtomicBoolean found` | Fast reads, visible to all threads |
| **State Storage** | `volatile` variables | Memory visibility across cores |
| **Timing** | `System.nanoTime()` | Nanosecond precision, monotonic |
| **Global Stop** | `Server1/2.stop_threads()` | Immediate propagation to all threads |
| **Reset Support** | `reset()` method | Multiple searches without restart |
| **Thread Safety** | Lock-free atomic operations | No contention, maximum performance |
| **Memory Usage** | ~80 bytes total | Minimal overhead |

---

## Key Takeaways for Presentation

1. **compareAndSet() - Atomic Winner Selection** - Guarantees only ONE thread wins even with simultaneous discoveries

2. **volatile Memory Visibility** - Ensures all threads see the `found` flag immediately across all CPU cores

3. **Lock-Free Design** - No synchronized blocks or locks = maximum performance and scalability

4. **Nanosecond Precision Timing** - Accurate measurement even for sub-second searches

5. **Global Coordination** - Single source of truth for search state across all threads and servers

6. **Immediate Stop Propagation** - Winner signals all threads to stop within 1-2 milliseconds

7. **Race Condition Prevention** - Atomic operations prevent multiple winners or lost updates

---

*SearchCoordinator demonstrates critical distributed systems concepts: atomic operations for consensus, memory visibility in multi-core systems, and lock-free coordination for maximum performance.*
