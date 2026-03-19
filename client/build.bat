@echo off
title OpenRune Client - Build
cd /d "%~dp0"

echo [INFO] OpenRune Client - Build
echo.

where javac >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if defined JAVA_HOME ( set "JAVAC=%JAVA_HOME%\bin\javac.exe" ) else (
        echo [ERROR] javac not found. Install JDK 11+ and add to PATH or set JAVA_HOME.
        pause
        exit /b 1
    )
) else ( set "JAVAC=javac" )

%JAVAC% -version 2>&1
echo.

if exist bin ( echo [INFO] Cleaning... & rmdir /s /q bin )
mkdir bin

set "CP=lib\xpp3-1.1.3.4.C.jar;lib\xstream-1.3.1.jar"
dir /s /b src\*.java > sources.txt
for /f %%A in ('type sources.txt ^| find /c /v ""') do echo [INFO] Compiling %%A files...

%JAVAC% -d bin -cp "%CP%" -encoding UTF-8 -nowarn @sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed.
    del sources.txt 2>nul
    pause
    exit /b 1
)
del sources.txt 2>nul
echo.
echo [SUCCESS] Build complete. Run with: launch.bat or run.bat
pause
