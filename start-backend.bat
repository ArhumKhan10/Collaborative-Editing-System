@echo off
echo ========================================
echo Starting Collaborative Editing System
echo ========================================
echo.

REM Check if MongoDB is running
echo [1/5] Checking MongoDB connection...
timeout /t 2 /nobreak >nul
echo MongoDB should be running on localhost:27017
echo.

REM Build all services first
echo [2/5] Building all services with Maven...
call mvn clean install -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven build failed!
    pause
    exit /b 1
)
echo Build completed successfully!
echo.

echo [3/5] Starting microservices...
echo.

REM Start API Gateway
echo Starting API Gateway on port 8080...
start "API Gateway :8080" cmd /k "cd api-gateway && mvn spring-boot:run"
timeout /t 10 /nobreak >nul

REM Start User Service
echo Starting User Service on port 8081...
start "User Service :8081" cmd /k "cd user-service && mvn spring-boot:run"
timeout /t 10 /nobreak >nul

REM Start Document Service
echo Starting Document Service on port 8082...
start "Document Service :8082" cmd /k "cd document-service && mvn spring-boot:run"
timeout /t 10 /nobreak >nul

REM Start Version Service
echo Starting Version Service on port 8083...
start "Version Service :8083" cmd /k "cd version-service && mvn spring-boot:run"
timeout /t 10 /nobreak >nul

echo.
echo [4/5] All backend services are starting...
echo.
echo Please wait for all services to fully start (30-60 seconds)
echo.
echo You can check service health at:
echo   - API Gateway:      http://localhost:8080/actuator/health
echo   - User Service:     http://localhost:8081/actuator/health
echo   - Document Service: http://localhost:8082/actuator/health
echo   - Version Service:  http://localhost:8083/actuator/health
echo.
echo Swagger UI available at:
echo   - User Service:     http://localhost:8081/swagger-ui.html
echo   - Document Service: http://localhost:8082/swagger-ui.html
echo   - Version Service:  http://localhost:8083/swagger-ui.html
echo.
echo [5/5] Backend startup complete!
echo.
echo To start the frontend, run: start-frontend.bat
echo.
pause
