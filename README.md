# Distributed MD5 Brute-Force Password Cracker

## TMN4013 Assignment 2 - Distributed Systems

A distributed and multithreaded MD5 password brute-force system using **Java RMI**.

---

## Project Overview

This system extends a single-machine multithreaded MD5 brute-force program into a **distributed client–server architecture** using Java RMI. The goal is to speed up password searching by distributing the workload across multiple servers and threads.

---

## Project Structure

```
src/main/java/
├── client/
│   └── BruteForceClient.java      # CLI client application
├── server/
│   ├── RMIServer.java             # RMI server entry point
│   ├── BruteForceServiceImpl.java # RMI service implementation
│   ├── SearchCoordinator.java     # Thread management & coordination
│   └── SearchThread.java          # Worker thread for brute-force
├── common/
│   ├── BruteForceService.java     # RMI remote interface
│   ├── SearchConfig.java          # Search configuration object
│   ├── SearchResult.java          # Search result object
│   └── SearchRange.java           # Search range object
└── util/
    ├── MD5Util.java               # MD5 hashing utilities
    ├── ServerLogger.java          # Server logging utility
    └── TestHashGenerator.java     # Test hash generator
```

---

## Requirements

- Java JDK 8 or higher
- No external dependencies (uses only Java standard library)

---

## How to Build

```cmd
build.bat
```

Or manually:
```cmd
mkdir target\classes
javac -d target\classes src\main\java\common\*.java src\main\java\util\*.java src\main\java\server\*.java src\main\java\client\*.java
```

---

## How to Run

### Step 1: Start RMI Server(s)

**Terminal 1 - Server 1:**
```cmd
start-server-1.bat
```

**Terminal 2 - Server 2 (optional):**
```cmd
start-server-2.bat
```

Wait until you see "RMI Server X is ready!"

### Step 2: Run the Client

**Terminal 3:**
```cmd
start-client.bat
```

---

## Client Prompts

1. **MD5 Hash** - The 32-character hexadecimal MD5 hash to crack
2. **Password Length** - Length of the password (1-6 characters)
3. **Number of Servers** - 1 or 2 servers
4. **Threads per Server** - Number of worker threads (1-16)

---

## Test Hashes

Generate test hashes:
```cmd
generate-test-hashes.bat
```

### Sample Test Cases (Character Set: a-z, 0-9)

| Password | Length | MD5 Hash                          |
|----------|--------|-----------------------------------|
| a        | 1      | 0cc175b9c0f1b6a831c399e269772661 |
| ab       | 2      | 187ef4436122d1cc2f40dc2b92f0eba0 |
| abc      | 3      | 900150983cd24fb0d6963f7d28e17f72 |
| test     | 4      | 098f6bcd4621d373cade4e832627b4f6 |
| xyz      | 3      | d16fb36f0911f878998c136191af705e |

---

## Features

✅ **Distributed Computing** with Java RMI  
✅ **Multithreaded** brute-force search  
✅ **Non-overlapping** work distribution  
✅ **Immediate termination** when password found  
✅ **Comprehensive logging** (server_1.log, server_2.log)  
✅ **Clean CLI** with input validation  
✅ **Error handling** for RMI and threading  

---

## Architecture

### Distributed Search Algorithm

1. **Client** connects to all configured RMI servers
2. **Search space** is divided equally among servers
3. Each **server** divides its range among worker threads
4. **Threads** perform brute-force search in parallel
5. When a match is found, all threads on all servers stop immediately

### Search Space Partitioning

```
Total Search Space (e.g., 46,656 for length 3)
    │
    ├── Server 1 (indices 0 - 23,327)
    │       ├── Thread 0: 0 - 5,831
    │       ├── Thread 1: 5,832 - 11,663
    │       └── ...
    │
    └── Server 2 (indices 23,328 - 46,655)
            ├── Thread 0: 23,328 - 29,159
            └── ...
```

---

## Log Files

- `server_1.log` - Server 1 operations
- `server_2.log` - Server 2 operations

Contains: Server start time, thread assignments, search progress, results, exceptions.

---

## Character Set

- Letters: a-z (26 characters)
- Digits: 0-9 (10 characters)
- **Total: 36 characters**

| Length | Combinations     |
|--------|------------------|
| 1      | 36               |
| 2      | 1,296            |
| 3      | 46,656           |
| 4      | 1,679,616        |
| 5      | 60,466,176       |
| 6      | 2,176,782,336    |

---

## Legacy Files (Original Single-Machine Version)
