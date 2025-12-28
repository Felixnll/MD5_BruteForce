@echo off
REM ============================================
REM Generate test MD5 hashes
REM ============================================

echo Generating test MD5 hashes...
echo.

java -cp target\classes util.TestHashGenerator
