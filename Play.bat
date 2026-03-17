@echo off
title OpenRune Launcher
cd /d "%~dp0"

echo ============================================
echo   OpenRune - 
echo   Starting server and client...
echo ============================================
echo.

:: Launch server in its own window
start "OpenRune Server" cmd /c "Start-Server.bat"

:: Wait for the server to boot before launching client
echo [OpenRune] Waiting 10 seconds for server startup...
timeout /t 10 /nobreak >nul

:: Launch client in its own window
start "OpenRune Client" cmd /c "Start-Client.bat"

echo [OpenRune] Both launched. You can close this window.
timeout /t 3 /nobreak >nul
