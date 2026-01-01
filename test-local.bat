@echo off
echo ================================================================
echo   LOCAL TEST - Run all components on ONE computer
echo ================================================================
echo.
echo This will open 3 windows:
echo   1. Server 1 (localhost)
echo   2. Server 2 (localhost)
echo   3. Client
echo.
echo QUICK TEST HASHES (found in seconds):
echo   6-char: afd845d6e734bb36477520cb5afca997  (password: !!!!!!)
echo   5-char: 952bccf9afe8e4c04306f70f7bed6610  (password: !!!!!)
echo   4-char: 98abe3a28383501f4bfd2d9077820f11  (password: !!!!)
echo   3-char: 6dd075556effaa6e7f1e3e3ba9fdc5fa  (password: !!!)
echo.
echo Press any key to start servers and client...
pause > nul

echo.
echo Starting Server 1...
start "Server 1" cmd /k "cd /d %~dp0 && java -cp target/classes server.RMIServer 1"

timeout /t 2 > nul

echo Starting Server 2...
start "Server 2" cmd /k "cd /d %~dp0 && java -cp target/classes server.RMIServer 2"

timeout /t 2 > nul

echo Starting Client...
start "Client" cmd /k "cd /d %~dp0 && java -cp target/classes client.BruteForceClient"

echo.
echo ================================================================
echo All components started!
echo.
echo In the Client window:
echo   1. Enter hash:    afd845d6e734bb36477520cb5afca997
echo   2. Password len:  6
echo   3. Servers:       2
echo   4. Server 1 IP:   localhost (press Enter)
echo   5. Server 2 IP:   localhost (press Enter)
echo   6. Threads:       5
echo.
echo The password "!!!!!!" should be found INSTANTLY by Server 1!
echo ================================================================
echo.
pause
