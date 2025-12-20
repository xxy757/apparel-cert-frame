@echo off
echo Checking frontend server status...
echo.
echo Port 3000 status:
netstat -ano | findstr ":3000"
echo.
echo Node processes:
tasklist | findstr "node.exe"
echo.
echo Press any key to exit...
pause >nul