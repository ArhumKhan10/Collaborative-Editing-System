@echo off
echo ========================================
echo Starting React Frontend
echo ========================================
echo.

cd frontend

REM Check if node_modules exists
if not exist "node_modules" (
    echo [1/2] Installing npm dependencies...
    call npm install
    if %ERRORLEVEL% NEQ 0 (
        echo ERROR: npm install failed!
        pause
        exit /b 1
    )
) else (
    echo [1/2] Dependencies already installed
)

echo.
echo [2/2] Starting development server...
echo.
echo Frontend will be available at: http://localhost:3000
echo.
echo Make sure backend services are running!
echo.

call npm run dev

pause
