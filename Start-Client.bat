@echo off
title OpenRune Client
cd /d "%~dp0"

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot

:: Check for gradle wrapper
if not exist "gradle\wrapper\gradle-wrapper.jar" (
    echo [OpenRune] Gradle wrapper missing, generating...
    call C:\gradle-8.6\bin\gradle.bat wrapper --gradle-version 8.6
    if errorlevel 1 (
        echo [OpenRune] Failed to generate wrapper. Is Gradle 8.6 installed at C:\gradle-8.6?
        pause
        exit /b 1
    )
)

:: Check for cache
if not exist "%USERPROFILE%\.openrune\cache\main_file_cache.dat" (
    echo [OpenRune] Cache not found, running setup...
    call setup-cache.bat
)

:: Build client
echo [OpenRune] Building client...
call .\gradlew.bat :client:compileJava -x test
if errorlevel 1 (
    echo [OpenRune] Build failed.
    pause
    exit /b 1
)

:: Run
echo.
echo ============================================
echo   OpenRune Client - 
echo   Make sure the server is running first!
echo ============================================
echo.
call .\gradlew.bat :client:run
pause
