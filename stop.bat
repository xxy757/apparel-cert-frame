@echo off
echo Stopping frontend development server...
echo Killing all node processes...

REM Kill all node processes
taskkill /f /im node.exe >nul 2>&1

if %ERRORLEVEL% == 0 (
    echo All node processes killed successfully!
) else (
    echo No node processes found or error killing processes.
)

echo.
echo Server stopped!
echo Press any key to exit...
pause >nul