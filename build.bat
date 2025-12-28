@echo off
REM ============================================
REM Build script for MD5 Brute-Force Project
REM TMN4013 Assignment 2 - Distributed System
REM ============================================

echo Building MD5 Brute-Force Distributed Project...
echo.

REM Create output directory
if not exist "target\classes" mkdir target\classes

REM Compile all Java files (including new RMI-based distributed packages)
echo Compiling Java files...
call "C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot\bin\javac" --release 8 -d target/classes ^
    src/main/java/common/*.java ^
    src/main/java/util/*.java ^
    src/main/java/server/*.java ^
    src/main/java/client/*.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Build successful!
    echo ========================================
    echo.
    echo TO RUN THE DISTRIBUTED SYSTEM:
    echo.
    echo Step 1 - Start RMI Server(s):
    echo   start-server-1.bat
    echo   start-server-2.bat  (in new terminal)
    echo.
    echo Step 2 - Run the Client:
    echo   start-client.bat
    echo.
    echo TO GENERATE TEST HASHES:
    echo   generate-test-hashes.bat
    echo ========================================
) else (
    echo.
    echo Build failed!
    exit /b 1
)
pause
