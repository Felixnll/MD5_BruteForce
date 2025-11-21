# MD5_BruteForce
Brute Force Attack using Java against MD5

## File Descriptions

### Main_Server.java
- Main entry point of the program
- Gets MD5 hash and thread count from user
- Splits threads between Server1 and Server2
- Starts both servers and waits for results
- Allows running multiple searches

### GlobalDistributor.java
- Manages work distribution across all threads
- Uses atomic counters to track progress
- Defines search space (ASCII 33-126, 94 characters)
- Distributes work in chunks of 1024

### SearchCoordinator.java
- Controls when password is found
- Stops all threads when password discovered
- Records timing and which thread found it
- Ensures only one thread reports success

### Server1.java
- Handles ASCII range 33-79 (first half)
- Creates and manages its threads
- Divides character range among threads
- Can start and stop threads

### Server2.java
- Handles ASCII range 80-126 (second half)
- Same structure as Server1
- Works with different character range
- Together with Server1, covers full range

### Search_Thread.java
- Does the actual password searching
- Tries all combinations from length 1-6
- Uses MD5 hashing to compare
- Stops when password found or told to stop

---

## Test Hashes

Group 3 MD5 hash
* 2-Char Password: 263a6fee6029b304bd1cf5ce0a782c6b
* 3-Char Password: 77aaa4dcce557f10d97b3ed037de33fb
* 4-Char Password: 9d64f0e38b080d131c1a27140df4e13b
* 5-Char Password: e76b29d2dfffb1a327d49a797d34c8a7
* 6-Char Password: f7808b86b6e53a97313f24a3619fdc95

## How to Run

1. Run the application: Execute `Main_Server`
2. Enter the MD5 hash when prompted
3. Enter number of threads (1-10)
4. Wait for results
5. After getting result, can choose between continuing the program or exit the program
