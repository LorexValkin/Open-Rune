@echo off
title OpenRune Client - Launch
cd /d "%~dp0"

echo ============================================
echo   OpenRune Client - Build ^& Launch
echo ============================================
echo.

:: Find Java
where javac >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if defined JAVA_HOME (
        set "JAVAC=%JAVA_HOME%\bin\javac.exe"
        set "JAVA=%JAVA_HOME%\bin\java.exe"
    ) else (
        echo [ERROR] javac not found. Install JDK 11+ and add to PATH or set JAVA_HOME.
        pause
        exit /b 1
    )
) else (
    set "JAVAC=javac"
    set "JAVA=java"
)

echo [INFO] Java version:
%JAVA% -version 2>&1 | findstr /i "version"
echo.

:: Check cache
set "CACHE_DIR=%USERPROFILE%\.openrune\cache"
if not exist "%CACHE_DIR%\main_file_cache.dat" (
    echo [WARNING] Cache not found at: %CACHE_DIR%\
    echo           Place main_file_cache.dat and .idx files there.
    echo.
)

:: Compile
set "CP_BUILD=lib\xpp3-1.1.3.4.C.jar;lib\xstream-1.3.1.jar"

set "NEEDS_BUILD=0"
if not exist bin\com\client\Client.class set "NEEDS_BUILD=1"
if "%1"=="--force" set "NEEDS_BUILD=1"
if "%1"=="-f" set "NEEDS_BUILD=1"

if "%NEEDS_BUILD%"=="1" (
    echo [INFO] Compiling client...
    if exist bin rmdir /s /q bin
    mkdir bin
    dir /s /b src\*.java > sources.txt
    %JAVAC% -d bin -cp "%CP_BUILD%" -encoding UTF-8 -nowarn @sources.txt 2>&1
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Compilation failed.
        del sources.txt 2>nul
        pause
        exit /b 1
    )
    del sources.txt 2>nul
    echo [INFO] Build OK.
    echo.
) else (
    echo [INFO] Using cached build. Use --force to rebuild.
    echo.
)

:: Launch
set "CP_RUN=bin;lib\xpp3-1.1.3.4.C.jar;lib\xstream-1.3.1.jar"

:: JVM flags ? full --add-opens for XStream on JDK 16+
set "JVM_FLAGS=-Xmx512m -Xms256m"
set "JVM_FLAGS=%JVM_FLAGS% --add-opens java.base/java.lang=ALL-UNNAMED"
set "JVM_FLAGS=%JVM_FLAGS% --add-opens java.base/java.lang.reflect=ALL-UNNAMED"
set "JVM_FLAGS=%JVM_FLAGS% --add-opens java.base/java.io=ALL-UNNAMED"
set "JVM_FLAGS=%JVM_FLAGS% --add-opens java.base/java.util=ALL-UNNAMED"
set "JVM_FLAGS=%JVM_FLAGS% --add-opens java.base/java.text=ALL-UNNAMED"
set "JVM_FLAGS=%JVM_FLAGS% --add-opens java.base/java.math=ALL-UNNAMED"
set "JVM_FLAGS=%JVM_FLAGS% --add-opens java.base/java.net=ALL-UNNAMED"
set "JVM_FLAGS=%JVM_FLAGS% --add-opens java.base/sun.net.www.protocol.http=ALL-UNNAMED"
set "JVM_FLAGS=%JVM_FLAGS% --add-opens java.desktop/java.awt=ALL-UNNAMED"
set "JVM_FLAGS=%JVM_FLAGS% --add-opens java.desktop/java.awt.font=ALL-UNNAMED"
set "JVM_FLAGS=%JVM_FLAGS% --add-opens java.desktop/java.awt.color=ALL-UNNAMED"
set "JVM_FLAGS=%JVM_FLAGS% --add-opens java.desktop/java.awt.event=ALL-UNNAMED"
set "JVM_FLAGS=%JVM_FLAGS% --add-opens java.desktop/java.awt.image=ALL-UNNAMED"
set "JVM_FLAGS=%JVM_FLAGS% --add-opens java.desktop/sun.awt=ALL-UNNAMED"
set "JVM_FLAGS=%JVM_FLAGS% --add-opens java.desktop/sun.awt.image=ALL-UNNAMED"
set "JVM_FLAGS=%JVM_FLAGS% --add-opens java.desktop/sun.java2d=ALL-UNNAMED"
set "JVM_FLAGS=%JVM_FLAGS% --add-opens java.desktop/javax.swing=ALL-UNNAMED"
set "JVM_FLAGS=%JVM_FLAGS% --add-opens java.desktop/javax.swing.plaf.basic=ALL-UNNAMED"

echo [INFO] Cache: %CACHE_DIR%\
echo [INFO] Server: 127.0.0.1:43594
echo [INFO] Launching OpenRune...
echo.

%JAVA% %JVM_FLAGS% -cp "%CP_RUN%" com.client.Client

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Client exited with error code %ERRORLEVEL%.
    pause
)
