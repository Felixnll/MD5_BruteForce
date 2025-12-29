@echo off
setlocal
echo ========================================
echo   Building MD5 BruteForce Project
echo ========================================
echo.

REM Save current directory
set "PROJECT_DIR=%CD%"

REM Create target directory if it doesn't exist
if not exist "target\classes" mkdir target\classes

REM Check if source folders exist
if not exist "src\main\java\common" (
    echo ERROR: Source folder 'src\main\java\common' not found!
    echo Make sure you extracted all project files correctly.
    pause
    exit /b 1
)

echo Compiling Java source files...
echo.

REM Try to find javac
set "JAVAC_CMD="

REM Check PATH first
where javac >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    set "JAVAC_CMD=javac"
    echo Using Java from system PATH...
    goto :compile
)

REM Try common locations
if exist "C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot\bin\javac.exe" (
    set "JAVAC_CMD=C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot\bin\javac.exe"
    echo Using Eclipse Adoptium JDK 25...
    goto :compile
)
if exist "C:\Program Files\Java\jdk-21\bin\javac.exe" (
    set "JAVAC_CMD=C:\Program Files\Java\jdk-21\bin\javac.exe"
    echo Using Oracle JDK 21...
    goto :compile
)
if exist "C:\Program Files\Java\jdk-17\bin\javac.exe" (
    set "JAVAC_CMD=C:\Program Files\Java\jdk-17\bin\javac.exe"
    echo Using Oracle JDK 17...
    goto :compile
)
if exist "C:\Program Files\Java\jdk-11\bin\javac.exe" (
    set "JAVAC_CMD=C:\Program Files\Java\jdk-11\bin\javac.exe"
    echo Using Oracle JDK 11...
    goto :compile
)

REM No javac found
echo.
echo ERROR: Java JDK not found!
echo.
echo Please install Java JDK from: https://adoptium.net/
echo Choose "Latest LTS" version and during installation,
echo check the option to add Java to PATH.
echo.
echo After installing, RESTART your computer and try again.
echo.
pause
exit /b 1

:compile
REM Use -sourcepath to let javac find all dependencies automatically
REM Compile main entry points which will pull in dependencies
"%JAVAC_CMD%" -sourcepath src/main/java -d target/classes src/main/java/server/RMIServer.java src/main/java/client/BruteForceClient.java src/main/java/util/TestHashGenerator.java 2>&1

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   Build successful!
    echo ========================================
    echo.
    echo You can now run:
    echo   - start-server-1.bat
    echo   - start-server-2.bat  
    echo   - start-client.bat
    echo.
) else (
    echo.
    echo ========================================
    echo   Build FAILED!
    echo ========================================
    echo.
    echo Check the error messages above.
    echo.
)
pause
