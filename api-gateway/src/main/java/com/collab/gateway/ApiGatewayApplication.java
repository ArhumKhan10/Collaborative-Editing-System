package com.collab.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway Application
 * 
 * Entry point for the API Gateway microservice.
 * Routes all client requests to appropriate backend services.
 * Handles global CORS, JWT validation, and request logging.
 * 
 * @author Arhum Khan
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.collab.gateway", "com.collab.common"})
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
