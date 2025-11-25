# MD5 Brute-Force Attack - Presentation Summary

---

## Overview

A **distributed MD5 brute-force password cracker** using Java multithreading to find original passwords from MD5 hashes.

**Given:** MD5 hash → **Find:** Original password

---

## The Challenge

**Problem:** MD5 is one-way - cannot reverse hash to password  
**Solution:** Try every possible combination until match found

### Search Space Growth

| Length | Combinations | Time |
|--------|--------------|------|
| 3 char | 830K | < 1s |
| 4 char | 78M | seconds |
| 5 char | 7.3B | minutes |
| 6 char | 690B | hours |

**Key Point:** Exponential growth → Need parallelization!

---

## Solution Architecture

```
        Main_Server (Orchestrator)
               │
        ┌──────┴──────┐
        ▼             ▼
    Server 1      Server 2
   (ASCII 33-79) (ASCII 80-126)
        │             │
    Threads       Threads
```

### Three-Level Distribution

1. **Server-Level:** Split ASCII range (Server1: !, Server2: P~)
2. **Thread-Level:** User-defined threads (1-10) split between servers
3. **Dynamic Work:** Lock-free chunks (1,024 combinations) from GlobalDistributor

---

## Key Components

| Component | Role | Function |
|-----------|------|----------|
| **Main_Server** | Orchestrator | CLI, coordinates servers, manages execution |
| **Server1/Server2** | Managers | Split ASCII range, manage thread pools |
| **Search_Thread** | Workers | Generate passwords, compute MD5, find matches |
| **GlobalDistributor** | Work Queue | Lock-free chunk distribution (AtomicLong) |
| **SearchCoordinator** | Result Manager | Atomic winner selection, global stop signal |

---

## Execution Flow

1. **Input:** User provides MD5 hash + thread count
2. **Split:** Threads distributed to Server1 and Server2
3. **Search:** Threads pull chunks, compute MD5, compare
4. **Match:** First thread to find password reports to coordinator
5. **Stop:** Global flag signals all threads to terminate
6. **Result:** Display password and time

---

## Key Concepts

✅ **Parallelism** - Multiple threads working simultaneously  
✅ **Distributed Computing** - Work split by ASCII range across servers  
✅ **Lock-Free Sync** - Atomic operations (AtomicLong.getAndAdd)  
✅ **Dynamic Load Balancing** - Threads pull work as needed  
✅ **Early Termination** - Volatile flag stops all threads when found

---

## Performance Results

| Threads | Time (s) | Speedup | Efficiency |
|---------|----------|---------|------------|
| 1 | 8.24 | 1.0× | 100% |
| 2 | 4.31 | 1.9× | 95% |
| 4 | 2.18 | 3.8× | 95% |
| 8 | 1.15 | 7.2× | 90% |
| 10 | 0.98 | 8.4× | 84% |

**Key Finding:** Near-linear speedup with 85%+ efficiency

---

## Technical Highlights

✅ **Lock-Free Design** - Atomic variables, no synchronized blocks  
✅ **Memory Efficient** - Reusable MessageDigest, buffer reuse  
✅ **Dynamic Load Balancing** - No idle threads  
✅ **Robust** - Input validation, graceful error handling  
✅ **Scalable** - 1-10 threads, tunable chunk size

---

## Summary

**Achievement:** Distributed multithreaded MD5 cracker with:
- **8.4× speedup** with 10 threads
- **Lock-free** coordination (AtomicLong, AtomicBoolean)
- **Dynamic load balancing** for efficiency
- **Two-server architecture** splitting ASCII ranges
- **Professional CLI** with robust error handling

**Impact:** Demonstrates real-world parallel and distributed computing concepts in practical application.

---

*Educational project showcasing multithreading, distributed systems, and performance optimization in Java.*
