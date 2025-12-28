@echo off
REM ============================================
REM Start RMI Server 1
REM ============================================

echo Starting RMI Server 1...
echo.

java -cp target\classes server.RMIServer 1
