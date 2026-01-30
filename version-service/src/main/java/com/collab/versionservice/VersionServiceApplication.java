package com.collab.versionservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Version Service Application
 * 
 * Entry point for the Version Control microservice.
 * Manages document version history, reversion, and user contribution tracking.
 * Integrates with MongoDB for version persistence.
 * 
 * @author Arhum Khan
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.collab.versionservice", "com.collab.common"})
@EnableMongoAuditing
@EnableScheduling
@OpenAPIDefinition(
    info = @Info(
        title = "Version Control Service API",
        version = "1.0.0",
        description = "API for document version history, revert, and contributions tracking",
        contact = @Contact(
            name = "Collaborative Editing System",
            email = "support@collab.com"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8083", description = "Version Service"),
        @Server(url = "http://localhost:8080", description = "API Gateway")
    }
)
public class VersionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VersionServiceApplication.class, args);
    }
}
