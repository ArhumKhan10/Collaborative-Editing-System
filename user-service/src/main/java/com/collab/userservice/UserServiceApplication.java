package com.collab.userservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * User Service Application
 * 
 * Entry point for the User Management microservice.
 * Handles user authentication, registration, and profile operations.
 * Integrates with MongoDB for user data persistence.
 * 
 * @author Arhum Khan
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.collab.userservice", "com.collab.common"})
@EnableMongoAuditing
@OpenAPIDefinition(
    info = @Info(
        title = "User Management Service API",
        version = "1.0.0",
        description = "API for user authentication, registration, and profile management",
        contact = @Contact(
            name = "Collaborative Editing System",
            email = "support@collab.com"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8081", description = "User Service"),
        @Server(url = "http://localhost:8080", description = "API Gateway")
    }
)
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
