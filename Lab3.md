# Lab 3

This material was adopted / modified from Sang Shin, www.javapassion.com
Lab 4: ThreadsGroup, View all threads, ThreadPriority
In this lab session, you are going to learn how to display information on a ThreadGroup, how to set a
thread priority, and so on.
I. Display threads of a ThreadGroup
II. Display all threads in the system
III. Set thread priority
I. Display threads of a ThreadGroup
1. Start the Notepad application, and code ThreadGroupTest.java as below:
2. Study the code by paying special attention to the bold parts.
public class ThreadGroupTest {
public static void main (String[] args) {
// Start three threads first. They should belong/ to a same ThreadsGroup.
new SimpleThread("Boston").start();
new SimpleThread("New York").start();
new SimpleThread("Seoul").start();
// Get ThreadGroup of the current thread and display
// the number of active threads that belong to the
// ThreadGroup.
ThreadGroup group = Thread.currentThread().getThreadGroup();
System.out.println("Number of active threads in this thread group = "
+ group.activeCount());
// Display the names of the threads in the current
// ThreadGroup.
Thread[] tarray = new Thread[10];
int actualSize = group.enumerate(tarray);
for (int i=0; i<actualSize;i++){
System.out.println("Thread " + tarray[i].getName()
+ " in thread group " + group.getName());
}
}
}
This material was adopted / modified from Sang Shin, www.javapassion.com
3. Start a new Notepad, and code SimpleThread.java as below.
public class SimpleThread extends Thread {
public SimpleThread(String str) {
super(str);
}
public void run() {
for (int i = 0; i < 5; i++) {
// System.out.format("%d %s%n", i, getName());
try {
sleep((long)(Math.random() * 1000));
} catch (InterruptedException e) {}
}
System.out.format("DONE! %s%n", getName());
}
}
4. Build and run the codes at terminal. Observe the result at the Terminal window.
Number of active threads in this thread group = 4
Thread main in thread group main
Thread Boston in thread group main
Thread New York in thread group main
Thread Seoul in thread group main
DONE! Seoul
DONE! New York
DONE! Boston
5. For your own exercise, do the following. Build and run the application.
• Modify ThreadGroupTest.java to create another (4th) SimpleThread instance using your
capital city of your country.
This material was adopted / modified from Sang Shin, www.javapassion.com
II. Display all threads in the system
1. Start the Notepad application, and code DisplayAllThreads.java as below. Study the
code by paying special attention to the bold sections.
public class DisplayAllThreads {
public static void main(String[] args) {
// Start three threads first. They should belong
// to a same ThreadsGroup.
new SimpleThread("Boston").start();
new SimpleThread("New York").start();
new SimpleThread("Seoul").start();
Thread[] tarray = findAllThreads();
for (int i=0; i<tarray.length;i++){
System.out.println("Thread " + tarray[i].getName()
+ " in thread group " + tarray[i].getThreadGroup().getName());
}
}
// Create an array of all threads in the system.
public static Thread[] findAllThreads() {
ThreadGroup group = Thread.currentThread().getThreadGroup();
ThreadGroup topGroup = group;
while (group != null) {
topGroup = group;
group = group.getParent();
}
int estimatedSize = topGroup.activeCount() * 2;
Thread[] slackList = new Thread[estimatedSize];
int actualSize = topGroup.enumerate(slackList);
Thread[] list = new Thread[actualSize];
System.arraycopy(slackList, 0, list, 0, actualSize);
return list;
}
}
This material was adopted / modified from Sang Shin, www.javapassion.com
2. Start a new Notepad, and code SimpleThread.java as below.
public class SimpleThread extends Thread {
public SimpleThread(String str) {
super(str);
}
public void run() {
for (int i = 0; i < 5; i++) {
// System.out.format("%d %s%n", i, getName());
try {
sleep((long)(Math.random() * 1000));
} catch (InterruptedException e) {}
}
System.out.format("DONE! %s%n", getName());
}
}
3. Build and run the codes at terminal. Observe the result at the Terminal window
Thread Reference Handler in thread group system
Thread Finalizer in thread group system
Thread Signal Dispatcher in thread group system
Thread main in thread group main
Thread Boston in thread group main
Thread New York in thread group main
Thread Seoul in thread group main
DONE! New York
DONE! Seoul
DONE! Boston
Figure 3.6: Result of running DisplayAllThreads application
4. For your own exercise, do the following. Build and run the application.
• Modify DisplayAllThreads.java to create another (4th) SimpleThread instance using your
capital city of your country.
This material was adopted / modified from Sang Shin, www.javapassion.com
III. Set thread priority
1. Start the Notepad application, and code ThreadsPriority.java below. Study the code by
paying special attention to the bold sections.
public class ThreadsPriority {
public static void main(String[] args) {
Thread t1 = new SimpleThread("Boston");
t1.start();
// Set the thread priority to 10(highest)
t1.setPriority(10);
Thread t2 = new SimpleThread("New York");
t2.start();
// Set the thread priority to 5
t2.setPriority(5);
Thread t3 = new SimpleThread("Seoul");
t3.start();
// Set the thread priority to 1
t3.setPriority(1);
}
}
2. Start a new Notepad, and code SimpleThread.java as below.
public class SimpleThread extends Thread {
public SimpleThread(String str) {
super(str);
}
public void run() {
for (int i = 0; i < 10; i++) {
System.out.println(i + " " + getName() + " Priority = " + getPriority());
}
System.out.println("Done! " + getName());
}
}
This material was adopted / modified from Sang Shin, www.javapassion.com
3. Build and run the codes at terminal. Observe the result at the Terminal window.
0 Boston Priority = 10
0 Seoul Priority = 1
0 New York Priority = 5
1 Boston Priority = 10
1 Seoul Priority = 1
1 New York Priority = 5
2 Boston Priority = 10
2 Seoul Priority = 1
3 Boston Priority = 10
2 New York Priority = 5
4 Boston Priority = 10
3 New York Priority = 5
5 Boston Priority = 10
6 Boston Priority = 10
7 Boston Priority = 10
8 Boston Priority = 10
9 Boston Priority = 10
Done! Boston
4 New York Priority = 5
5 New York Priority = 5
6 New York Priority = 5
7 New York Priority = 5
8 New York Priority = 5
9 New York Priority = 5
Done! New York
3 Seoul Priority = 1
4 Seoul Priority = 1
5 Seoul Priority = 1
6 Seoul Priority = 1
7 Seoul Priority = 1
8 Seoul Priority = 1
9 Seoul Priority = 1
Done! Seoul
Summary
In this lab session, you have learned how to retrieve information and prioritize the thread processing on
a ThreadGroup.