@echo off
title OpenRune Build
cd /d "%~dp0"

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot

:: Check for gradle wrapper
if not exist "gradle\wrapper\gradle-wrapper.jar" (
    echo [OpenRune] Gradle wrapper missing, generating...
    call C:\gradle-8.6\bin\gradle.bat wrapper --gradle-version 8.6
    if errorlevel 1 (
        echo [OpenRune] Failed to generate wrapper.
        pause
        exit /b 1
    )
)

echo [OpenRune] Building all modules...
call .\gradlew.bat build -x test
if errorlevel 1 (
    echo [OpenRune] Build failed.
    pause
    exit /b 1
)

echo.
echo [OpenRune] Build complete.
pause
