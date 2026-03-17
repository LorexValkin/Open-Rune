@echo off
echo  Building OpenRune...
echo.
call gradlew.bat build -x test --no-daemon
if %ERRORLEVEL% equ 0 (
    echo.
    echo  Build successful!
) else (
    echo.
    echo  Build failed! Check errors above.
)
pause
