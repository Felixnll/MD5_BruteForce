# 🔐 MD5 Brute-Force Project - Teammate Guide

## TMN4013 Assignment 2: Distributed & Multithreaded Password Cracker

---

## 📋 Prerequisites

Before running, make sure you have:
- **Java JDK 8 or higher** installed
- All project files extracted to a folder

---

## 🚀 How to Run the Project

### Step 1: Build the Project
Double-click `build.bat` to compile all Java files.
- You should see "Build successful!" message

### Step 2: Start the Servers (MUST DO FIRST!)
**Important:** Servers must be running BEFORE the client!

- Double-click `start-server-1.bat` → Opens Server 1
- Double-click `start-server-2.bat` → Opens Server 2

Each server window will show:
```
========================================
  MD5 BRUTE-FORCE RMI SERVER 1
========================================
Server IP Address: 192.168.x.x
----------------------------------------
Remote clients can connect using: 192.168.x.x
========================================
```

### Step 3: Start the Client
Double-click `start-client.bat`

When prompted:
1. Enter server IP address (press Enter for `localhost` if testing on same PC)
2. Enter the MD5 hash you want to crack
3. Enter password length (1-6)
4. Enter number of servers (1 or 2)
5. Enter threads per server (1-16, recommended: 4-8)

---

## 🖥️ Different Setup Options

### Option A: Single PC (Localhost) - For Testing
```
Your PC
├── Server 1 (start-server-1.bat)
├── Server 2 (start-server-2.bat)
└── Client (start-client.bat) → Enter: localhost
```

### Option B: Virtual Machine (Linux VM on same PC)
```
Your PC (Windows)
├── Server 1 (start-server-1.bat)
├── Server 2 (start-server-2.bat)
│
└── Linux VM
    └── Client → Enter: Host PC's IP (e.g., 192.168.x.x)
```

### Option C: Different PCs (Same Network/WiFi)
```
PC 1 (Server Machine)          PC 2 (Client Machine)
├── Server 1                   └── Client
├── Server 2                       → Enter: PC1's IP
└── Run open-firewall.bat            (e.g., 192.168.1.100)
    (as Administrator!)
```

### Option D: Remote (Different Networks) - Using Hamachi
```
Person A's Home                Person B's Home
├── Install Hamachi            ├── Install Hamachi
├── Create/Join network        ├── Join same network
├── Server 1 & 2               └── Client
├── open-firewall.bat              → Enter: Person A's
└── Note Hamachi IP                  Hamachi IP (25.x.x.x)
    (25.x.x.x)
```

---

## 🔥 Firewall Setup (Server Machine Only!)

**Only the person running the SERVERS needs to do this:**

1. Right-click `open-firewall.bat`
2. Select "Run as administrator"
3. Click "Yes" when prompted

This allows incoming connections on port 1099 (RMI port).

---

## 🌐 Remote Connection Setup (Different Networks)

If you're NOT on the same WiFi/network, use **Hamachi**:

### Setting up Hamachi:
1. Download from: https://vpn.net/
2. Install and create an account
3. One person: Create a network (Network → Create new network)
4. Share the **Network ID** and **Password** with teammates
5. Others: Join the network (Network → Join existing network)
6. Everyone gets a virtual IP like `25.48.123.45`

### After Hamachi Setup:
- Server person: Note your Hamachi IP (shown in Hamachi window)
- Client person: Use that Hamachi IP when connecting

---

## 🎯 MD5 Hashes to Crack (Assignment)

| Password Length | MD5 Hash |
|-----------------|----------|
| 2 characters | `263a6fee6029b304bd1cf5ce0a782c6b` |
| 3 characters | `77aaa4dcce557f10d97b3ed037de33fb` |
| 4 characters | `9d64f0e38b080d131c1a27140df4e13b` |
| 5 characters | `e76b29d2dfffb1a327d49a797d34c8a7` |
| 6 characters | `f7808b86b6e53a97313f24a3619fdc95` |

---

## 📊 Understanding the Output

When cracking, you'll see:
```
Starting distributed search...
════════════════════════════════════════════════════════════════
  [Server 1] Searching: !! to O&
  [Server 2] Searching: O' to ~~
════════════════════════════════════════════════════════════════

PASSWORD FOUND!
════════════════════════════════════════════════════════════════
  Password:    abc123
  Found by:    Server 1
  Time taken:  2.45 seconds
════════════════════════════════════════════════════════════════
```

---

## ❓ Troubleshooting

### "Connection refused" or "Server is DOWN"
- Make sure servers are running BEFORE starting client
- Check if you entered the correct IP address
- Run `open-firewall.bat` as Admin on server machine

### "Server not registered in RMI registry"
- Server might still be starting up
- Wait 5-10 seconds and try again

### Can't connect remotely
- Both must be on same network OR use Hamachi
- Firewall must be open on server machine
- Try pinging the server IP to test connectivity

### Build fails
- Make sure Java JDK is installed
- Check that JAVA_HOME is set correctly

---

## 📁 Project Files

| File | Purpose |
|------|---------|
| `build.bat` | Compiles all Java files |
| `start-server-1.bat` | Starts RMI Server 1 |
| `start-server-2.bat` | Starts RMI Server 2 |
| `start-client.bat` | Starts the client application |
| `open-firewall.bat` | Opens Windows firewall for RMI (run as Admin) |

---

## 👥 Team Roles (Suggestion)

For testing with 5 team members:
- **1 person**: Run both servers (or 2 people run 1 server each)
- **1 person**: Run the client
- **Others**: Observe and document results

---

## 💡 Tips

1. Start with **localhost** testing first to make sure everything works
2. Use **4-8 threads per server** for good performance
3. Shorter passwords (2-3 chars) crack in seconds, longer ones take more time
4. The character set includes 94 characters (A-Z, a-z, 0-9, and symbols)

---

Good luck with the assignment! 🚀
