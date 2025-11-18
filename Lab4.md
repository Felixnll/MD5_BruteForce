# lab 4

This material was adopted / modified from Sang Shin, www.javapassion.com
Lab 4: Thread Synchronization
In this lab session, you are going to exercise how to do synchronization among threads.
1. Build and run a program in which threads are NOT synchronized
2. Build an run a program in which threads are synchronized through synchronized method
3. Build and run a program in which threads are synchronized through synchronized
statement on a common object
1. Build and run a program in which threads are NOT synchronized
In this step, you are going to build an application that displays a result that is not desirable since threads
are not synchronized.
a) Start the Notepad application, and code UnsynchronizedExample.java as below.
public class UnsynchronizedExample {
public static void main(String[] args) {
new PrintStringsThread("Hello ", "there.");
new PrintStringsThread("How are ", "you?");
new PrintStringsThread("Thank you ", "very much!");
}
}
b) Start a new Notepad application, and code PrintStringsThread.java as below.
public class PrintStringsThread implements Runnable {
Thread thread;
String str1, str2;
PrintStringsThread(String str1, String str2) {
this.str1 = str1;
this.str2 = str2;
thread = new Thread(this);
thread.start();
}
public void run() {
TwoStrings.print(str1, str2);
}
}
c) Start a new Notepad application, and code TwoStrings.java as below. Note that the print
method is not synchronized.
This material was adopted / modified from Sang Shin, www.javapassion.com
public class TwoStrings {
// This method is not synchronized
static void print(String str1, String str2) {
System.out.print(str1);
try {
Thread.sleep(500);
} catch (InterruptedException ie) {
}
System.out.println(str2);
}
}
d) Build and run the codes at the terminal. Observe the result in the terminal.
Hello How are Thank you there.
very much!
you?
This material was adopted / modified from Sang Shin, www.javapassion.com
2. Build and run a program in which threads are synchronized through synchronized method
In this step, you are going to build an application that displays a desired result because the threads are
synchronized.
a) Start the Notepad application, and code SynchronizedExample1.java below.
public class SynchronizedExample1 {
public static void main(String[] args) {
new PrintStringsThread("Hello ", "there.");
new PrintStringsThread("How are ", "you?");
new PrintStringsThread("Thank you ", "very much!");
}
}
b) Start a new Notepad application, and code PrintStringsThread.java as below.
public class PrintStringsThread implements Runnable {
Thread thread;
String str1, str2;
PrintStringsThread(String str1, String str2) {
this.str1 = str1;
this.str2 = str2;
thread = new Thread(this);
thread.start();
}
public void run() {
TwoStrings.print(str1, str2);
}
}
c) Start a new Notepad application, and code TwoStrings.java as below.
public class TwoStrings {
// This method is now synchronized
synchronized static void print(String str1, String str2) {
System.out.print(str1);
try {
Thread.sleep(500);
} catch (InterruptedException ie) {
}
System.out.println(str2);
}
}
d) Build and run the codes at terminal. Observe the result at the terminal.
How are you?
Thank you very much!
Hello there.
This material was adopted / modified from Sang Shin, www.javapassion.com
3. Build and run a program in which threads are synchronized through synchronized
statement on common object
In this step, you are going to build another application that displays a desired result because the
threads are synchronized.
a) Start the Notepad application, and code SynchronizedExample2.java as below.
public class SynchronizedExample2 {
public static void main(String[] args) {
TwoStrings ts = new TwoStrings();
new PrintStringsThread("Hello ", "there.", ts);
new PrintStringsThread("How are ", "you?", ts);
new PrintStringsThread("Thank you ", "very much!", ts);
}
}
b) Start a new Notepad application, and code PrintStringsThread.java as below. Observe the
highlighted codes.
public class PrintStringsThread implements Runnable {
Thread thread;
String str1, str2;
TwoStrings ts;
PrintStringsThread(String str1, String str2,
TwoStrings ts) {
this.str1 = str1;
this.str2 = str2;
this.ts = ts;
thread = new Thread(this);
thread.start();
}
public void run() {
// Synchronize over TwoString object
synchronized (ts) {
ts.print(str1, str2);
}
}
}
This material was adopted / modified from Sang Shin, www.javapassion.com
c) Start a new Notepad application, and code TwoStrings.java as below.
public class TwoStrings {
static void print(String str1, String str2) {
System.out.print(str1);
try {
Thread.sleep(500);
} catch (InterruptedException ie) {
}
System.out.println(str2);
}
}
d) Build and run the codes at terminal. Observe the result at the terminal.
How are you?
Thank you very much!
Hello there.
Summary
In this lab session, you have learned how to synchronize Java threads.