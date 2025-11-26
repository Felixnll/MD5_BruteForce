@echo off
echo Building the project...
call "C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot\bin\javac" --release 8 -d target/classes src/main/java/MD5_BruteForce/*.java

if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Starting Main Server...
call "C:\Program Files\Java\jre1.8.0_471\bin\java.exe" -cp target/classes MD5_BruteForce.Main_Server
pause
