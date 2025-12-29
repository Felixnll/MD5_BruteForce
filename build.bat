@echo off
echo Building MD5 BruteForce project...
call "C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot\bin\javac" --release 8 -d target/classes src/main/java/MD5_BruteForce/*.java

if %ERRORLEVEL% EQU 0 (
    echo Build successful!
) else (
    echo Build failed!
)
pause
