@echo off
echo Starting frontend development server...
echo Checking port 3000...

REM Check if port 3000 is in use
netstat -ano | findstr ":3000" >nul
if %ERRORLEVEL% == 0 (
    echo Port 3000 is in use, killing process...
    REM Get and kill the process using port 3000
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3000"') do (
        taskkill /f /pid %%a >nul 2>&1
        echo Killed process %%a
    )
    REM Wait for port to be released
    timeout /t 1 /nobreak >nul
) else (
    echo Port 3000 is available
)

echo Starting server...
start "Frontend Dev Server" cmd /k "cd /d %~dp0frontend && npm run dev"
echo.
echo Server started in new window!
echo Access URL: http://localhost:3000
echo Press any key to exit...
pause >nul