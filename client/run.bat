@echo off
title OpenRune Client
cd /d "%~dp0"

where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if defined JAVA_HOME ( set "JAVA=%JAVA_HOME%\bin\java.exe" ) else (
        echo [ERROR] java not found. Install JDK 11+ and add to PATH or set JAVA_HOME.
        pause
        exit /b 1
    )
) else ( set "JAVA=java" )

if not exist bin\com\client\Client.class (
    echo [ERROR] Client not compiled. Run launch.bat first.
    pause
    exit /b 1
)

set "CP=bin;lib\xpp3-1.1.3.4.C.jar;lib\xstream-1.3.1.jar"

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

echo [INFO] Launching OpenRune...
%JAVA% %JVM_FLAGS% -cp "%CP%" com.client.Client

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Client exited with error code %ERRORLEVEL%.
    pause
)
