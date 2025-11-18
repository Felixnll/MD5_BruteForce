# Lab 2

This material was adopted / modified from Sang Shin, www.javapassion.com
Lab 3: Implement Runnable interface
In this exercise, you are going to create and start a thread by writing a class that implements Runnable
interface.
I. Create and start a thread by implementing Runnable interface - start() method is not in the
constructor
II. Create and start a thread by implementing Runnable interface - start() method is in the
constructor
I. Create and start a thread by implementing Runnable interface - start() method is not in the
constructor
1. Start the Notepad application, and code RunnableThreadTest1.java as below:
2. Study the code by paying special attention to the bold parts. Note that the start() method needs
to be invoked explicitly after an object instance of the PrintNameRunnable class is created.
3. Start a new Notepad application, and code PrintNameRunnable.java as below:
public class RunnableThreadTest1 {
public static void main(String args[]) {
PrintNameRunnable1 pnt1 = new PrintNameRunnable("A");
Thread t1 = new Thread(pnt1);
t1.start();
PrintNameRunnable1 pnt2 = new PrintNameRunnable("B");
Thread t2 = new Thread(pnt2);
t2.start();
PrintNameRunnable1 pnt3 = new PrintNameRunnable("C");
Thread t3 = new Thread(pnt3);
t3.start();
}
}
// The class implements Runnable interface
class PrintNameRunnable1 implements Runnable {
String name;
PrintNameRunnable1(String name) {
this.name = name;
}
// Implementation of the run() defined in the
// Runnable interface.
public void run() {
for (int i = 0; i < 10; i++) {
System.out.print(name);
}
}
}
This material was adopted / modified from Sang Shin, www.javapassion.com
4. Build and run the codes at the terminal. Observe the result in the terminal window.
5. For your own exercise, do the following. Build and run the application.
• Create another class called MyOwnRunnableClass that implements Runnable interface
• MyOwnRunnableClass displays values 1 to 10 inside its run() method
• Modify RunnableThreadTest1.java to start 2 thread instances of MyOwnRunnableClass.
This material was adopted / modified from Sang Shin, www.javapassion.com
II. Create and start a thread by implementing Runnable interface - start() method is in the constructor
1. Start the Notepad application, and code RunnableThreadTest2.java as below:
2. Start a new Notepad, and code PrintNameRunnable.java as below:
3. Note that the start() method is in the constructor of the PrintNameRunnable class.
4. Build and run the codes at terminal. Observe the result at the Terminal window.
public class RunnableThreadTest2 {
public static void main(String args[]) {
// Since the constructor of the PrintNameRunnable
// object creates a Thread object and starts it,
// there is no need to do it here.
new PrintNameRunnable2("A");
new PrintNameRunnable2("B");
new PrintNameRunnable2("C");
}
}
// The class implements Runnable interface
class PrintNameRunnable2 implements Runnable {
Thread thread;
PrintNameRunnable2(String name) {
thread = new Thread(this, name);
thread.start();
}
// Implementation of the run() defined in the
// Runnable interface.
public void run() {
String name = thread.getName();
for (int i = 0; i < 10; i++) {
System.out.print(name);
}
}
}
This material was adopted / modified from Sang Shin, www.javapassion.com
For your own exercise, do the following. Build and run the application.
• Create another class called MyOwnRunnableClass that implements Runnable interface
• MyOwnRunnableClass displays values 1 to 10 inside its run() method
• Modify RunnableThreadTest2.java to start 2 thread instances of MyOwnRunnableClass.
Summary
In this lab session, you have learned how to create a class that implements Runnable interface and
start a thread.