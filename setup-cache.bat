@echo off
REM ============================================================
REM  OpenRune - Cache Setup
REM  Copies 317 cache files to %USERPROFILE%\.openrune\cache\
REM ============================================================
setlocal

set "CACHE_SOURCE=cache-data"
set "CACHE_DEST=%USERPROFILE%\.openrune\cache"

echo.
echo  =============================================
echo   OpenRune Cache Setup
echo  =============================================
echo.

REM Check if source cache exists
if not exist "%CACHE_SOURCE%" (
    echo  [ERROR] No cache folder found at: %CACHE_SOURCE%
    echo.
    echo  Place your 317 cache files in the "cache" folder:
    echo    %CD%\cache\
    echo.
    echo  Required files:
    echo    main_file_cache.dat
    echo    main_file_cache.idx0
    echo    main_file_cache.idx1
    echo    main_file_cache.idx2
    echo    main_file_cache.idx3
    echo    main_file_cache.idx4
    echo.
    pause
    exit /b 1
)

REM Create destination
if not exist "%CACHE_DEST%" (
    echo  Creating cache directory: %CACHE_DEST%
    mkdir "%CACHE_DEST%"
)

echo  Copying cache files...
echo    From: %CACHE_SOURCE%
echo    To:   %CACHE_DEST%
echo.

REM Copy everything recursively
xcopy /E /Y /Q "%CACHE_SOURCE%\*" "%CACHE_DEST%\" >nul 2>&1

REM Verify key files
if exist "%CACHE_DEST%\main_file_cache.dat" (
    echo  [OK] main_file_cache.dat
) else (
    echo  [MISSING] main_file_cache.dat
)

for %%i in (0 1 2 3 4) do (
    if exist "%CACHE_DEST%\main_file_cache.idx%%i" (
        echo  [OK] main_file_cache.idx%%i
    ) else (
        echo  [MISSING] main_file_cache.idx%%i
    )
)

echo.
echo  Cache setup complete!
echo  The client will now load from: %CACHE_DEST%
echo.
pause
