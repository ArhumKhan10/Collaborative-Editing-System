@echo off
echo ========================================
echo Stopping All Services
echo ========================================
echo.

echo Killing all Java processes (Maven Spring Boot)...
taskkill /F /FI "IMAGENAME eq java.exe" 2>nul
if %ERRORLEVEL% EQU 0 (
    echo Java processes stopped
) else (
    echo No Java processes found
)

echo.
echo Killing all Node processes (React)...
taskkill /F /FI "IMAGENAME eq node.exe" 2>nul
if %ERRORLEVEL% EQU 0 (
    echo Node processes stopped
) else (
    echo No Node processes found
)

echo.
echo All services stopped!
echo.
pause
