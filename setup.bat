@echo off
REM ============================================================
REM  OpenRune - First Time Setup (Windows)
REM  Run this ONCE after cloning the repo
REM ============================================================
setlocal enabledelayedexpansion

echo.
echo  =============================================
echo   OpenRune - First Time Setup
echo  =============================================
echo.

REM --- Check Java ---
where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo  [ERROR] Java not found!
    echo.
    echo  Install JDK 21 from: https://adoptium.net/temurin/releases/?version=21
    echo  During install, CHECK "Set JAVA_HOME variable"
    echo  After installing, close and reopen this terminal.
    echo.
    pause
    exit /b 1
)

echo  [OK] Java found:
java -version 2>&1 | findstr /i "version"
echo.

REM --- Download Gradle Wrapper JAR if missing ---
if not exist "gradle\wrapper\gradle-wrapper.jar" (
    echo  [..] Downloading Gradle wrapper...
    if not exist "gradle\wrapper" mkdir "gradle\wrapper"

    powershell -ExecutionPolicy Bypass -Command ^
        "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; ^
         $url = 'https://services.gradle.org/distributions/gradle-8.6-bin.zip'; ^
         $zip = Join-Path $env:TEMP 'gradle-8.6-bin.zip'; ^
         $dir = Join-Path $env:TEMP 'gradle-8.6'; ^
         Write-Host '  Downloading Gradle 8.6...'; ^
         Invoke-WebRequest -Uri $url -OutFile $zip -UseBasicParsing; ^
         Write-Host '  Extracting...'; ^
         Expand-Archive -Path $zip -DestinationPath $env:TEMP -Force; ^
         $wrapperJar = Join-Path $dir 'lib\gradle-wrapper-8.6.jar'; ^
         if (Test-Path (Join-Path $dir 'bin\gradle.bat')) { ^
             Write-Host '  Generating wrapper...'; ^
             Push-Location '%CD%'; ^
             & (Join-Path $dir 'bin\gradle.bat') wrapper --gradle-version 8.6; ^
             Pop-Location; ^
         } else { ^
             Write-Host '  Using direct wrapper download...'; ^
             Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/gradle/gradle/v8.6.0/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle\wrapper\gradle-wrapper.jar' -UseBasicParsing; ^
         } ^
         Remove-Item $zip -Force -ErrorAction SilentlyContinue"

    if not exist "gradle\wrapper\gradle-wrapper.jar" (
        echo.
        echo  [ERROR] Could not download Gradle wrapper automatically.
        echo.
        echo  Manual fix:
        echo    1. Download Gradle 8.6 from https://gradle.org/releases/
        echo    2. Extract it somewhere (e.g. C:\gradle-8.6)
        echo    3. Open a terminal in this project folder
        echo    4. Run: C:\gradle-8.6\bin\gradle.bat wrapper --gradle-version 8.6
        echo    5. Run setup.bat again
        echo.
        pause
        exit /b 1
    )
    echo  [OK] Gradle wrapper ready.
) else (
    echo  [OK] Gradle wrapper already present.
)
echo.

REM --- Create data directories if missing ---
if not exist "data\config" mkdir "data\config"
if not exist "data\saves" mkdir "data\saves"
if not exist "server\plugins" mkdir "server\plugins"
if not exist "logs" mkdir "logs"

REM --- Build ---
echo  [..] Building OpenRune (first build downloads dependencies, ~2-3 min)...
echo.
call gradlew.bat build -x test --no-daemon

if %ERRORLEVEL% neq 0 (
    echo.
    echo  [ERROR] Build failed! Common fixes:
    echo    - Make sure JAVA_HOME points to JDK 21 (not JRE)
    echo    - Make sure you have internet (Gradle downloads dependencies)
    echo    - Try: gradlew.bat build -x test --stacktrace
    echo.
    pause
    exit /b 1
)

echo.
echo  =============================================
echo   Setup Complete!
echo  =============================================
echo.
echo   Start the server:    start-server.bat
echo   Start the client:    start-client.bat
echo   Build after changes: build.bat
echo.
pause
