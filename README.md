# MD5_BruteForce
Brute Force Attack using Java against MD5

- Search_Thread : this class has the main thread to start searching the password , it receives an interval
of search characters for the first symbole and use them for search


- Server1 , Server2 : these classes while receiving the number N , they divide the intervals of search for 
the first character and run N threads each one with an interval of search , once of them finds out the password
it stops all other threads


- Main_Server : this is the main class , ( the one you should run ) , it receives the hashcode , and the number N
from the user input , then start both servers

---------------------
*Advice for testing :
better use N=4 threads [ optimal time ]
and use these hashcodes for testing before using assignements hashcode ( just to make sure everything alright ) :
* bf6871d4fdbe9c0955bf304eaa06c640
* 821f40e6beabbc20876d3e0e9ed2bef7
* 6766f4262b2c600eddcf5461c7e9938a
  263a6fee6029b304bd1cf5ce0a782c6b

  //---------------------------------

* 263a6fee6029b304bd1cf5ce0a782c6b
* 77aaa4dcce557f10d97b3ed037de33fb
* 9d64f0e38b080d131c1a27140df4e13b
* e76b29d2dfffb1a327d49a797d34c8a7
* f7808b86b6e53a97313f24a3619fdc95
* $input = "f7808b86b6e53a97313f24a3619fdc95`n10"; $input | & 'C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot\bin\java.exe' -cp target\classes MD5_BruteForce.Main_Server
