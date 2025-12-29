@echo off
REM ============================================
REM Open Windows Firewall for RMI (Port 1099)
REM RUN AS ADMINISTRATOR
REM ============================================

echo.
echo This script will open port 1099 for Java RMI connections.
echo You must run this as Administrator!
echo.

REM Allow inbound connections on port 1099
netsh advfirewall firewall add rule name="Java RMI Server (Port 1099)" dir=in action=allow protocol=tcp localport=1099

REM Allow Java to communicate through firewall
netsh advfirewall firewall add rule name="Java RMI - Java.exe" dir=in action=allow program="C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot\bin\java.exe" enable=yes

echo.
echo ========================================
echo Firewall rules added successfully!
echo Port 1099 is now open for RMI.
echo ========================================
echo.
pause
