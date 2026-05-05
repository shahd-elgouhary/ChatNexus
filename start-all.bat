@echo off
echo ====================================
echo Starting ChatNexus System
echo ====================================

:: Kill any existing Java processes to ensure a clean start
echo [1/3] Terminating any running servers...
taskkill /F /IM java.exe >nul 2>&1

:: Build the project again just to be safe
echo [2/3] Building the latest code...
call build.bat
if errorlevel 1 (
    echo [ERROR] Build failed! Check errors above.
    pause
    exit /b 1
)

:: Run the server directly in the foreground for debugging!
echo [3/3] Starting Server in Foreground...
echo ====================================
echo SERVER LOGS BELOW:
echo ====================================
java -jar server.jar 1234 localhost root root
pause
