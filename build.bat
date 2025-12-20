@echo off
echo Building frontend project...
cd /d "%~dp0frontend"
npm run build
echo.
echo Build completed!
echo Press any key to exit...
pause >nul