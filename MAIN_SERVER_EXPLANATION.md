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

✅ **Three-Level Validation**:
1. **Null check**: Handles EOF gracefully
2. **Format check**: `parseInt()` ensures it's a valid integer
3. **Range check**: Must be 1-10 (reasonable for demo/testing)

✅ **Exception Handling**:
```java
try {
    totalThreads = Integer.parseInt(threadsLine);
} catch (NumberFormatException nfe) {
    // User entered "abc" or "1.5" or similar
    System.err.println("Error: Invalid number format...");
    continue;  // Re-prompt instead of crash
}
```

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
// Collect all threads from both servers
joinList.addAll(Server1.threads);
joinList.addAll(Server2.threads);
// Join all threads (wait for them to complete)
for (Thread t : joinList) {
    try {
        t.join();  // this blocks until thread finishes
    } catch (InterruptedException e) {
        // Handle interruption properly
        Thread.currentThread().interrupt();
    }
}

System.out.println();
System.out.println("Search finished.");
```

### Why This is Important:

✅ **Thread.join() - Waiting for Completion**:
```java
t.join();  // Main thread waits here until thread t finishes
```
- Blocks the main thread until worker thread completes
- Ensures all threads finish before displaying "Search finished"
- Without this, program would exit while threads still running

**Flow Diagram:**
```
Main Thread                     Worker Threads
     │                          ├─ Thread 1 (searching...)
     │                          ├─ Thread 2 (searching...)
     ├─ Start threads           ├─ Thread 3 (searching...)
     │                          └─ Thread 4 (searching...)
     ├─ Call t.join() ──────────→ (waiting for all to finish)
     │   (blocked here)         
     │                          Thread 2 finds password!
     │                          └─ Exits
     │                          Other threads see stop flag
     │                          └─ All exit
     ← All threads done ─────────┘
     │
     ├─ "Search finished"
```

✅ **Proper Interrupt Handling**:
```java
catch (InterruptedException e) {
    Thread.currentThread().interrupt();  // Restore interrupt status
}
```
- If main thread interrupted, preserves the interrupt flag
- Allows graceful handling of Ctrl+C or similar signals
- Best practice for thread interruption

✅ **Collecting Threads from Both Servers**:
```java
joinList.addAll(Server1.threads);
joinList.addAll(Server2.threads);
```
- Ensures we wait for ALL threads regardless of which server they belong to
- Handles asymmetric thread distribution (e.g., 5 on Server1, 3 on Server2)

---

## 5. Re-Run Prompt and Loop Control

**Lines 106-133**

```java
// Ask if user wants to search another hash
while (true) {
    System.out.print("Run another hash? (Y (Yes)/N (No)) ");
    String resp = sc.nextLine();
    if (resp == null) {  // EOF or ctrl+d
        System.out.println();
        System.out.println("No input detected. Exiting.");
        waitForExit(sc);
        sc.close();
        return;
    }
    resp = resp.trim();  // clean up the response
    // Check if user wants to exit
    if (resp.isEmpty() || resp.equalsIgnoreCase("n") || resp.equalsIgnoreCase("no")) {
        System.out.println("Exiting program.");
        waitForExit(sc);
        sc.close();
        return;
    }
    // If they said yes, continue to get new hash
    if (resp.equalsIgnoreCase("y") || resp.equalsIgnoreCase("yes")) {
        break;  // exit this loop, continue to get new hash
    }
    // Invalid response, ask again
    System.out.println("Please answer 'y' or 'n'.");
}
```

### Why This is Important:

✅ **Interactive Multi-Run Feature**:
- Allows testing multiple hashes without restarting program
- Saves time (no need to recompile or restart JVM)
- Professional user experience

✅ **Flexible Input Handling**:
```java
resp.equalsIgnoreCase("y")     // accepts: y, Y
resp.equalsIgnoreCase("yes")   // accepts: yes, Yes, YES
resp.equalsIgnoreCase("n")     // accepts: n, N
resp.equalsIgnoreCase("no")    // accepts: no, No, NO
resp.isEmpty()                 // accepts: just pressing Enter = exit
```

✅ **Re-Prompt on Invalid Input**:
```java
System.out.println("Please answer 'y' or 'n'.");
// Loop continues, asks again
```
- Doesn't crash on typos
- Clear guidance to user

**User Experience Flow:**
```
Search finished.
Run another hash? (Y (Yes)/N (No)) maybe
Please answer 'y' or 'n'.
Run another hash? (Y (Yes)/N (No)) y

Enter the hashcode --> [user enters new hash]
```

---

## 6. Subsequent Hash Input (Re-Run Loop)

**Lines 135-151**

```java
// Get the new hash to search
String newHash;
while (true) {
    System.out.print("Enter the hashcode --> ");
    newHash = sc.nextLine();
    if (newHash == null) {
        System.out.println();
        System.out.println("No input detected. Exiting.");
        waitForExit(sc);
        sc.close();
        return;
    }
    newHash = newHash.trim();
    if (newHash.length() == 32 && newHash.matches("[0-9a-fA-F]{32}")) {
        break;
    }
    System.err.println("Error: Invalid MD5 hash format. MD5 hashes must be 32 hexadecimal characters. Please try again.");
}
hashcode = newHash;  // update the hashcode for next iteration
```

### Why This is Important:

✅ **Same Validation as Initial Input**:
- Ensures consistency across multiple runs
- Prevents invalid hashes in subsequent searches
- User experience remains predictable

✅ **Variable Update**:
```java
hashcode = newHash;
```
- Updates the `hashcode` variable for the next loop iteration
- Next search will use this new hash

✅ **Full Re-Validation**:
- Doesn't assume user will be more careful on 2nd+ input
- Same strict validation every time
- Maintains robustness

---

## 7. Subsequent Thread Count Input (Re-Run Loop)

**Lines 153-173**

```java
// Get new thread count for next search
while (true) {
    System.out.print("Enter the total number of threads to use (1-10) --> ");
    String threadsLine = sc.nextLine();
    if (threadsLine == null) {
        System.out.println();
        System.out.println("No input detected. Exiting.");
        waitForExit(sc);
        sc.close();
        return;
    }
    threadsLine = threadsLine.trim();
    try {
        totalThreads = Integer.parseInt(threadsLine);
    } catch (NumberFormatException nfe) {
        System.err.println("Error: Invalid number format for total threads. Please try again.");
        continue;
    }
    if (totalThreads < 1 || totalThreads > 10) {
        System.err.println("Error: Number of threads must be between 1 and 10 (inclusive). Please try again.");
        continue;
    }
    break;
}
```

### Why This is Important:

✅ **Allows Performance Testing**:
- User can try same hash with different thread counts
- Compare 1 thread vs 5 threads vs 10 threads
- Empirical performance analysis

✅ **Same Validation Logic**:
- Identical to initial thread count input
- Consistency and robustness maintained

**Example Testing Scenario:**
```
Run 1: 5f4dcc3b... with 1 thread  → Time: 10 seconds
Run 2: 5f4dcc3b... with 5 threads → Time: 2.5 seconds
Run 3: 5f4dcc3b... with 10 threads → Time: 1.8 seconds
→ User observes diminishing returns after 5 threads
```

---

## 8. Exit Handler - Wait for User

**Lines 178-191**

```java
private static void waitForExit(Scanner sc) {
    System.out.println();
    System.out.println("Press Enter to quit.");
    // Keep waiting until user presses enter
    while (true) {
        String line = sc.nextLine();
        if (line == null) break;  // EOF
        line = line.trim();
        if (line.isEmpty() || line.equalsIgnoreCase("exit")) break;  // exit conditions
        System.out.println("Type 'exit' or press Enter to quit.");
    }
}
```

### Why This is Important:

✅ **Prevents Console Window from Closing**:
- On Windows, console windows close immediately when program exits
- User can't read results if window disappears instantly
- `waitForExit()` keeps window open until user ready

✅ **Graceful Termination**:
- User has time to read final results
- Can take screenshots or copy output
- Professional user experience

✅ **Multiple Exit Options**:
```java
line.isEmpty()              // Just press Enter
line.equalsIgnoreCase("exit")  // Type "exit"
line == null                // Ctrl+D (EOF)
```

**User Experience:**
```
Search finished.
Run another hash? (Y (Yes)/N (No)) n
Exiting program.

Press Enter to quit.
[User reviews results, then presses Enter]
[Program exits]
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
