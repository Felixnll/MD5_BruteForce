# Lab 1

This material was adopted / modified from Sang Shin, www.javapassion.com
Lab 2: Extending Thread Class
In this lab session, you are going to learn how to create and start a thread execution by writing
a class that extends Thread class. You will learn how to start the thread either by not having
the start() method in the constructor of the subclass or having it in the constructor method of
the subclass.
I. The start() method is not in the constructor of the subclass
II. The start() method is in the constructor of the subclass
I. The start() method is not in the constructor of the subclass
1. Start the Notepad application, and code ExtendThreadClassTest.java as below:
2. Note that the start() is invoked after the object instance of PrintNameThread class is created.
3. Start a new Notepad application, and code the PrintNameThread.java as below.
// Subclass extends Thread class
public class PrintNameThread extends Thread {
PrintNameThread(String name) {
super(name);
}
// Override the run() method of the Thread class.
// This method gets executed when start() method
// is invoked.
public void run() {
System.out.println("run() method of the " + this.getName() + " thread is called" );
for (int i = 0; i < 10; i++) {
System.out.print(this.getName());
}
}
}
public class ExtendThreadClassTest {
public static void main(String args[]) {
// Create object instance of a class that is subclass of Thread class
System.out.println("Creating PrintNameThread object instance..");
PrintNameThread pnt1 = new PrintNameThread("A");
// Start the thread by invoking start() method
System.out.println("Calling start() method of " + pnt1.getName() + " thread");
pnt1.start();
}
}
This material was adopted / modified from Sang Shin, www.javapassion.com
4. Build and run the codes at the terminal. Observe the result in the terminal.
5. Modify the ExtendThreadClassTest.java as below. The code fragments that need to be added
are highlighted in bold and blue-colored codes.
6. Build and run the codes at terminal. Observe the result at the terminal.
7. For your own exercise, modify ExtendThreadClassTest.java as following. Build and run the code.
• Create and start another thread.
• Set the name of the thread as "MyOwn"
public class ExtendThreadClassTest {
public static void main(String args[]) {
// Create object instance of a class that is subclass of Thread class
System.out.println("Creating PrintNameThread object instance..");
PrintNameThread pnt1 = new PrintNameThread("A");
// Start the thread by invoking start() method
System.out.println("Calling start() method of " + pnt1.getName() + " thread");
pnt1.start();
System.out.println("Creating PrintNameThread object instance..");
PrintNameThread pnt2 = new PrintNameThread("B");
System.out.println("Calling start() method of " + pnt2.getName() + " thread");
pnt2.start();
System.out.println("Creating PrintNameThread object instance..");
PrintNameThread pnt3 = new PrintNameThread("C");
System.out.println("Calling start() method of " + pnt3.getName() + " thread");
pnt3.start();
}
}
This material was adopted / modified from Sang Shin, www.javapassion.com
II. The start() method is in the constructor of the subclass
1. Start the Notepad application, and code ExtendThreadClassTest2.java as below:
2. Start a new Notepad, and code PrintNameThread.java as below.
Note that the start() method is invoked as part of the constructor method of the
PrintNameThread class.
3. Build and run the codes at terminal. Observe the result at the Terminal window.
4. For your own exercise, modify ExtendThreadClassTest2.java as following. Build and run the
application.
• Create and start another thread.
• Set the name of the thread as "MyOwn"
Summary
In this lab session, you have learned how to create and start a thread by extending Thread class.
public class ExtendThreadClassTest2 {
public static void main(String args[]) {
PrintNameThread pnt1 = new PrintNameThread("A");
PrintNameThread pnt2 = new PrintNameThread("B");
PrintNameThread pnt3 = new PrintNameThread("C");
}
}
public class PrintNameThread extends Thread {
PrintNameThread(String name) {
super(name);
// start() method is inside the constructor of the subclass
start();
}
public void run() {
String name = getName();
for (int i = 0; i < 10; i++) {
System.out.print(name);
}
}
}