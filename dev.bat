@echo off
title OpenRune - Dev Launcher
cd /d "%~dp0"

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot"

echo ============================================
echo   OpenRune Dev Launcher
echo ============================================
echo.

echo [1/5] Killing running instances...
taskkill /F /IM java.exe >nul 2>&1
call .\gradlew.bat --stop >nul 2>&1
echo        Done.
echo.

echo [2/5] Building server...
call .\gradlew.bat :core:clean :core:build -x test
if errorlevel 1 (
    echo [ERROR] Server build failed.
    pause
    exit /b 1
)
echo        Server build OK.
echo.

echo [3/5] Copying plugin JARs...
for /d %%d in (plugins\*) do (
    if exist "%%d\build\libs" (
        copy /y "%%d\build\libs\*.jar" "server\plugins\" >nul 2>&1
    )
)
echo        Done.
echo.

echo [4/5] Building client...
if exist "client\bin" rmdir /s /q "client\bin"
mkdir "client\bin"
dir /s /b "client\src\*.java" > "%TEMP%\openrune-sources.txt"
"%JAVA_HOME%\bin\javac.exe" -d "client\bin" -cp "client\lib\xpp3-1.1.3.4.C.jar;client\lib\xstream-1.3.1.jar" -encoding UTF-8 -nowarn @"%TEMP%\openrune-sources.txt" 2>&1
if errorlevel 1 (
    echo [ERROR] Client build failed.
    del "%TEMP%\openrune-sources.txt" 2>nul
    pause
    exit /b 1
)
del "%TEMP%\openrune-sources.txt" 2>nul
echo        Client build OK.
echo.

echo [5/5] Launching...
echo.

:: Launch server in new window
start "OpenRune Server" cmd /c "cd /d "%~dp0" && .\gradlew.bat :core:run && pause"

:: Wait for server
echo   Waiting for server...
:waitloop
timeout /t 1 /nobreak >nul
netstat -an 2>nul | findstr "43594.*LISTENING" >nul 2>&1
if errorlevel 1 goto waitloop
echo   Server online!
echo.

:: Launch client in new window
set "CP=client\bin;client\lib\xpp3-1.1.3.4.C.jar;client\lib\xstream-1.3.1.jar"
set "JVM=-Xmx512m -Xms256m"
set "JVM=%JVM% --add-opens java.base/java.lang=ALL-UNNAMED"
set "JVM=%JVM% --add-opens java.base/java.lang.reflect=ALL-UNNAMED"
set "JVM=%JVM% --add-opens java.base/java.io=ALL-UNNAMED"
set "JVM=%JVM% --add-opens java.base/java.util=ALL-UNNAMED"
set "JVM=%JVM% --add-opens java.base/java.text=ALL-UNNAMED"
set "JVM=%JVM% --add-opens java.base/java.math=ALL-UNNAMED"
set "JVM=%JVM% --add-opens java.base/java.net=ALL-UNNAMED"
set "JVM=%JVM% --add-opens java.base/sun.net.www.protocol.http=ALL-UNNAMED"
set "JVM=%JVM% --add-opens java.desktop/java.awt=ALL-UNNAMED"
set "JVM=%JVM% --add-opens java.desktop/java.awt.font=ALL-UNNAMED"
set "JVM=%JVM% --add-opens java.desktop/java.awt.color=ALL-UNNAMED"
set "JVM=%JVM% --add-opens java.desktop/java.awt.event=ALL-UNNAMED"
set "JVM=%JVM% --add-opens java.desktop/java.awt.image=ALL-UNNAMED"
set "JVM=%JVM% --add-opens java.desktop/sun.awt=ALL-UNNAMED"
set "JVM=%JVM% --add-opens java.desktop/sun.awt.image=ALL-UNNAMED"
set "JVM=%JVM% --add-opens java.desktop/sun.java2d=ALL-UNNAMED"
set "JVM=%JVM% --add-opens java.desktop/javax.swing=ALL-UNNAMED"
set "JVM=%JVM% --add-opens java.desktop/javax.swing.plaf.basic=ALL-UNNAMED"

start "OpenRune Client" "%JAVA_HOME%\bin\java.exe" %JVM% -cp "%CP%" com.client.Client

echo.
echo ============================================
echo   Both running!
echo ============================================
pause
