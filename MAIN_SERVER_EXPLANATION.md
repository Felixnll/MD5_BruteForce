# Main_Server.java - Code Explanation

This document explains the key code blocks and their importance in the `Main_Server.java` orchestration class.

---

## Overview

`Main_Server` is the entry point and orchestrator of the entire MD5 brute-force search system. It provides the command-line interface (CLI), handles user input validation, coordinates the startup of Server1 and Server2, manages the re-run loop for multiple consecutive searches, and ensures graceful program termination. This class ties all components together into a cohesive, user-friendly application.

---

## 1. Initial Hash Input and Validation

**Lines 18-34**

```java
// Get the MD5 hash from user
String hashcode;
// Keep asking until we get valid input
while (true) {
    System.out.print("Enter the hashcode --> ");
    hashcode = sc.nextLine();
    if (hashcode == null) {  // check for null input (EOF)
        System.out.println();
        System.out.println("No input detected. Exiting.");
        waitForExit(sc);
        sc.close();
        return;
    }
    hashcode = hashcode.trim();  // remove extra spaces
    // MD5 hash should be exactly 32 characters and only contain hex digits
    if (hashcode.length() == 32 && hashcode.matches("[0-9a-fA-F]{32}")) {
        break;  // valid hash, exit loop
    }
    System.err.println("Error: Invalid MD5 hash format. MD5 hashes must be 32 hexadecimal characters. Please try again.");
}
```

### Why This is Important:

✅ **Robust Input Validation**:
- Checks for null input (handles Ctrl+D/Ctrl+Z EOF)
- Trims whitespace to handle copy-paste errors
- Validates exactly 32 characters (MD5 hash length)
- Ensures only hexadecimal characters (0-9, a-f, A-F)

✅ **Regular Expression Validation**:
```java
hashcode.matches("[0-9a-fA-F]{32}")
```
- `[0-9a-fA-F]` = any hex digit
- `{32}` = exactly 32 characters
- Rejects: "123" (too short), "xyz..." (non-hex), "12345...67890abc" (too long)

✅ **User-Friendly Error Handling**:
- Clear error messages explain what went wrong
- Re-prompts instead of crashing
- Prevents invalid data from entering the system

**Example Valid Inputs:**
```
5f4dcc3b5aa765d61d8327deb882cf99  ✓ (password)
098f6bcd4621d373cade4e832627b4f6  ✓ (test)
5D41402ABC4B2A76B9719D911017C592  ✓ (hello, uppercase OK)
```

**Example Invalid Inputs:**
```
5f4dcc3b5aa765d61d8327deb882cf9   ✗ (31 chars, too short)
5f4dcc3b5aa765d61d8327deb882cf999 ✗ (33 chars, too long)
xyz4dcc3b5aa765d61d8327deb882cf99  ✗ (contains non-hex 'x', 'y', 'z')
```

---

## 2. Thread Count Input and Validation

**Lines 37-61**

```java
// Get number of threads to use
int totalThreads;
// Input validation loop for thread count
while (true) {
    System.out.print("Enter the total number of threads to use (1-10) --> ");
    String threadsLine = sc.nextLine();
    if (threadsLine == null) {  // EOF check
        System.out.println();
        System.out.println("No input detected. Exiting.");
        waitForExit(sc);
        sc.close();
        return;
    }
    threadsLine = threadsLine.trim();
    // Try to convert string to integer
    try {
        totalThreads = Integer.parseInt(threadsLine);
    } catch (NumberFormatException nfe) {  // if not a valid number
        System.err.println("Error: Invalid number format for total threads. Please try again.");
        continue;
    }
    // Make sure thread count is in valid range
    if (totalThreads < 1 || totalThreads > 10) {
        System.err.println("Error: Number of threads must be between 1 and 10 (inclusive). Please try again.");
        continue;  // ask again
    }
    break;  // valid input received
}
```

### Why This is Important:

✅ **Four-Level Validation**:

1. **Null Check** - Handles EOF (Ctrl+D/Ctrl+Z), exits gracefully
2. **Format Check** - `parseInt()` validates it's a valid integer
3. **Range Check** - Ensures threads are 1-10 (prevents system overload)
4. **Exception Handling** - Catches `NumberFormatException` for invalid formats (e.g., "abc", "5.5"), re-prompts instead of crash

✅ **User-Friendly Constraints**:
- Minimum 1 thread (at least one worker)
- Maximum 10 threads (prevents system overload in demo environment)
- Clear error messages for each violation

**Example Valid Inputs:**
```
5   ✓ (middle range)
1   ✓ (minimum)
10  ✓ (maximum)
```

**Example Invalid Inputs:**
```
0    ✗ (below minimum)
15   ✗ (above maximum)
abc  ✗ (not a number)
5.5  ✗ (not an integer)
```

---

## 3. Search Initialization and Execution

**Lines 66-87**

```java
// Main execution loop - allows running multiple searches
while (true) {
    System.out.println();
    System.out.println("Starting brute force attack...");
    System.out.println("Hash: " + hashcode);

    // Split threads between servers (Server1 gets more if odd number)
    int threadsServer1 = (totalThreads + 1) / 2;
    int threadsServer2 = totalThreads - threadsServer1;
    System.out.println("Total threads: " + totalThreads + " (Server1=" + threadsServer1 + ", Server2=" + threadsServer2 + ")");
    System.out.println();

    // Reset everything before starting new search
    SearchCoordinator.reset();
    GlobalDistributor.init();
    SearchCoordinator.setStartTime(System.nanoTime());

    // Start both servers
    Server1.start_server(hashcode, threadsServer1);
    if (threadsServer2 > 0) {  // only start server 2 if it has threads
        Server2.start_server(hashcode, threadsServer2);
    }
```

### Why This is Important:

✅ **Thread Distribution Strategy**:
```java
int threadsServer1 = (totalThreads + 1) / 2;
int threadsServer2 = totalThreads - threadsServer1;
```

**Distribution Examples:**
```
totalThreads = 10:
  threadsServer1 = (10 + 1) / 2 = 5
  threadsServer2 = 10 - 5 = 5
  Result: 5 + 5 = 10 ✓

totalThreads = 7 (odd):
  threadsServer1 = (7 + 1) / 2 = 4
  threadsServer2 = 7 - 4 = 3
  Result: 4 + 3 = 7 ✓ (Server1 gets extra)

totalThreads = 1:
  threadsServer1 = (1 + 1) / 2 = 1
  threadsServer2 = 1 - 1 = 0
  Result: Only Server1 runs
```

✅ **Clean State Initialization**:
```java
SearchCoordinator.reset();      // Clear previous search state
GlobalDistributor.init();       // Reset counters to 0
SearchCoordinator.setStartTime(System.nanoTime());  // Start timer
```
- Ensures no leftover data from previous run
- Critical for accurate timing
- Enables multiple consecutive searches

✅ **Conditional Server2 Startup**:
```java
if (threadsServer2 > 0) {
    Server2.start_server(hashcode, threadsServer2);
}
```
- Handles edge case where totalThreads = 1
- Avoids starting Server2 with 0 threads
- Saves resources

---

## 4. Thread Synchronization and Waiting

**Lines 89-104**

```java
// Wait for all threads to finish
List<Thread> joinList = new java.util.ArrayList<>();
joinList.addAll(Server1.threads);
joinList.addAll(Server2.threads);
for (Thread t : joinList) {
    try {
        t.join();  // Wait until thread finishes
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}
System.out.println("Search finished.");
```

### For Canva Presentation:

**What is Thread.join()?**
- Main thread **waits** until all worker threads complete
- Prevents "Search finished" from displaying too early
- Like waiting for all students to finish exam before collecting papers

**Three Steps:**

1. **Collect Threads**
   - Gather all threads from Server1 and Server2 into one list
   - Ensures we wait for ALL workers (e.g., 8 threads total)

2. **Wait for Completion**
   - Call `join()` on each thread
   - Main thread **blocks** (freezes) until that thread finishes
   - Repeats for all threads in the list

3. **Display Result**
   - Only after ALL threads done → Print "Search finished"
   - Guarantees correct timing and output

**Why This Matters:**
- ❌ Without join: Program exits while threads still working
- ✅ With join: Clean synchronization and accurate results

**Real Example:**
- Time 0s: Start 8 threads
- Time 5s: Main calls join() → WAITS
- Time 10s: Password found, threads exit
- Time 12s: Main unblocked → "Search finished" ✓

---

## 5. User Experience - Multi-Run & Exit Handling

### Re-Run Loop + Input Re-validation + Exit Handler

**Purpose:** Allow users to test multiple hashes without restarting the program

**Key Features:**

1. **Multi-Run Capability**
   - Asks: "Run another hash? (Y/N)"
   - Accepts: y, yes, n, no, Enter (case-insensitive)
   - Invalid input → Re-prompts with guidance

2. **Input Re-validation**
   - New hash: Same 32-char hex validation as initial input
   - New thread count: Same 1-10 range validation
   - Maintains consistency and robustness across multiple runs

3. **Exit Handler (waitForExit)**
   - Prevents console window from closing immediately (Windows)
   - User can review results before exit
   - Multiple exit options: Enter, "exit", or Ctrl+D

**Benefits:**
- ✅ No restart needed for performance testing
- ✅ Compare different thread counts easily
- ✅ Professional user experience
- ✅ Time to review/screenshot results

**Example Flow:**
```
Search 1 → "Run another?" → Yes → Search 2 → "Run another?" → No → Exit
```

---

## Complete Program Flow Diagram

```
┌─────────────────────────────────────────────────────┐
│ START                                               │
└────────────────┬────────────────────────────────────┘
                 ▼
    ┌────────────────────────┐
    │ Get MD5 hash (validate)│
    └────────────┬───────────┘
                 ▼
    ┌────────────────────────────┐
    │ Get thread count (validate)│
    └────────────┬───────────────┘
                 ▼
    ┌────────────────────────────┐
    │ MAIN LOOP START            │
    └────────────┬───────────────┘
                 ▼
    ┌────────────────────────────┐
    │ Reset coordinators         │
    │ • SearchCoordinator.reset()│
    │ • GlobalDistributor.init() │
    └────────────┬───────────────┘
                 ▼
    ┌────────────────────────────┐
    │ Split threads to servers   │
    │ Server1: ceil(n/2)         │
    │ Server2: floor(n/2)        │
    └────────────┬───────────────┘
                 ▼
    ┌────────────────────────────┐
    │ Start Server1 & Server2    │
    │ (create and start threads) │
    └────────────┬───────────────┘
                 ▼
    ┌────────────────────────────┐
    │ Wait for all threads       │
    │ (Thread.join())            │
    └────────────┬───────────────┘
                 ▼
    ┌────────────────────────────┐
    │ Display "Search finished"  │
    └────────────┬───────────────┘
                 ▼
    ┌────────────────────────────┐
    │ "Run another hash?" (Y/N)  │
    └────────────┬───────────────┘
                 │
         ┌───────┴───────┐
         ▼               ▼
    ┌────────┐      ┌─────────┐
    │   NO   │      │   YES   │
    └────┬───┘      └────┬────┘
         │               │
         │               ▼
         │     ┌──────────────────┐
         │     │ Get new hash     │
         │     │ Get new threads  │
         │     └────┬─────────────┘
         │          │
         │          └──────┐
         │                 │
         │                 ▼
         │     ┌───────────────────┐
         │     │ LOOP BACK TO TOP  │
         │     └───────────────────┘
         │
         ▼
┌────────────────┐
│ waitForExit()  │
│ (Press Enter)  │
└────────┬───────┘
         ▼
    ┌────────┐
    │  EXIT  │
    └────────┘
```

---

## Error Handling Strategies

### 1. Input Validation Loops

**Pattern Used Throughout:**
```java
while (true) {
    // Get input
    // Validate
    if (valid) break;
    // Show error and re-prompt
}
```

✅ **Benefits:**
- Never crashes on bad input
- Clear error messages
- User-friendly recovery

### 2. EOF (End-of-File) Handling

**Every Input Check:**
```java
if (input == null) {
    System.out.println("No input detected. Exiting.");
    waitForExit(sc);
    sc.close();
    return;
}
```

✅ **Handles:**
- Ctrl+D on Unix/Linux
- Ctrl+Z on Windows
- Piped input ending
- Input redirection ending

### 3. Exception Handling

**Number Parsing:**
```java
try {
    totalThreads = Integer.parseInt(threadsLine);
} catch (NumberFormatException nfe) {
    System.err.println("Error: Invalid number format...");
    continue;
}
```

✅ **Graceful Degradation:**
- Catches parsing errors
- Explains problem to user
- Continues execution

### 4. Range Validation

**Explicit Bounds Checking:**
```java
if (totalThreads < 1 || totalThreads > 10) {
    System.err.println("Error: Number must be between 1 and 10...");
    continue;
}
```

✅ **Prevents:**
- System overload (too many threads)
- Invalid configurations (0 or negative threads)

---

## Key Design Patterns

### 1. Separation of Concerns

```
Main_Server:      User interface, orchestration
Server1/Server2:  Thread management, range splitting
Search_Thread:    Actual MD5 computation
GlobalDistributor: Work distribution
SearchCoordinator: Result coordination
```

### 2. Validation at Boundaries

- All user input validated before entering system
- No invalid data propagates to core logic
- Clear separation between UI and business logic

### 3. Graceful Error Recovery

- No crashes on invalid input
- Clear error messages
- Re-prompt instead of exit

### 4. User Experience Focus

- Interactive prompts
- Multiple consecutive runs
- Wait-for-exit to view results
- Clear progress messages

---

## Performance Considerations

### Overhead Analysis:

```
Input validation:     ~microseconds (negligible)
Thread distribution:  ~microseconds (simple arithmetic)
Server startup:       ~milliseconds (thread creation)
Thread joining:       ~milliseconds after search done
Total overhead:       < 100 milliseconds

Actual search time:   seconds to hours
Overhead percentage:  < 0.01% of total time
```

**Conclusion:** All Main_Server operations are extremely fast compared to actual MD5 search time.

---

## Common Questions

**Q: Why split threads with (totalThreads + 1) / 2 instead of / 2?**  
A: `(n+1)/2` gives ceiling division for Server1, ensuring it gets the extra thread for odd numbers. Example: 7 threads → Server1 gets 4, Server2 gets 3.

**Q: Why allow multiple searches without restart?**  
A: Convenience for testing and performance analysis. User can quickly test multiple hashes or thread counts.

**Q: What happens if user enters hash during search?**  
A: Program is blocked at `t.join()` waiting for threads, so won't read input until search completes.

**Q: Why limit to 10 threads?**  
A: Reasonable for demo/educational purposes. Production could increase limit, but more threads ≠ always faster (depends on CPU cores).

**Q: What if Server2 gets 0 threads?**  
A: The `if (threadsServer2 > 0)` check prevents starting Server2 with 0 threads. Only Server1 runs.

**Q: Why use nextLine() everywhere instead of next() or nextInt()?**  
A: `nextLine()` consumes the entire line including newline, preventing input buffer issues. Also allows null checking for EOF.

**Q: Can program run in non-interactive mode (pipe input)?**  
A: Yes! The null checks handle EOF, so can pipe: `echo -e "hash\n5" | java Main_Server`

---

## Example Session

### Complete Interactive Session:

```
Enter the hashcode --> 5f4dcc3b5aa765d61d8327deb882cf99
Enter the total number of threads to use (1-10) --> 8

Starting brute force attack...
Hash: 5f4dcc3b5aa765d61d8327deb882cf99
Total threads: 8 (Server1=4, Server2=4)

Server 1 starting...
Thread 0 (Server 1) assigned interval 33-45 -> ascii [33..44] ('!'...',')
Thread 1 (Server 1) assigned interval 45-56 -> ascii [45..55] ('-'...'7')
Thread 2 (Server 1) assigned interval 56-68 -> ascii [56..67] ('8'...'C')
Thread 3 (Server 1) assigned interval 68-80 -> ascii [68..79] ('D'...'O')
Thread 0 (Server 2) assigned interval 80-92 -> ascii [80..91] ('P'...'[')
Thread 1 (Server 2) assigned interval 92-104 -> ascii [92..103] ('\'...'g')
Thread 2 (Server 2) assigned interval 104-115 -> ascii [104..114] ('h'...'r')
Thread 3 (Server 2) assigned interval 115-127 -> ascii [115..126] ('s'...'~')
Thread 0 on Server 1 searching length 1
Thread 1 on Server 1 searching length 1
[... search progresses ...]
Password found : password by Thread 2 on Server 1
Time : 42.196734 seconds (0.703279 minutes)

Search finished.
Run another hash? (Y (Yes)/N (No)) y
Enter the hashcode --> 098f6bcd4621d373cade4e832627b4f6
Enter the total number of threads to use (1-10) --> 4

Starting brute force attack...
Hash: 098f6bcd4621d373cade4e832627b4f6
Total threads: 4 (Server1=2, Server2=2)
[... search progresses ...]
Password found : test by Thread 0 on Server 2
Time : 0.012345 seconds (0.000206 minutes)

Search finished.
Run another hash? (Y (Yes)/N (No)) n
Exiting program.

Press Enter to quit.
[User presses Enter]
[Program exits]
```

---

## Key Takeaways for Presentation

1. **Robust CLI with Full Input Validation** - Regex validation, range checking, exception handling, EOF handling

2. **Interactive Multi-Run Feature** - Test multiple hashes without restart, performance comparison

3. **Smart Thread Distribution** - Automatic split between Server1/Server2 using ceiling division

4. **Coordinated Initialization** - Resets all coordinators before each search for clean state

5. **Thread Synchronization** - Uses Thread.join() to wait for all workers to complete

6. **Graceful Error Handling** - Re-prompts on invalid input, never crashes

7. **User Experience Focus** - Wait-for-exit, clear messages, flexible input formats

8. **Orchestration Layer** - Ties all components together: coordinators, servers, threads

---

*Main_Server demonstrates professional CLI application design: comprehensive input validation, robust error handling, user-friendly interaction, and clean orchestration of distributed components.*
