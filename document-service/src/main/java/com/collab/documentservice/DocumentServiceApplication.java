package com.collab.documentservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication(scanBasePackages = {"com.collab.documentservice", "com.collab.common"})
@EnableMongoAuditing
@OpenAPIDefinition(
    info = @Info(
        title = "Document Editing Service API",
        version = "1.0.0",
        description = "API for document creation, editing, sharing, and real-time collaboration",
        contact = @Contact(
            name = "Collaborative Editing System",
            email = "support@collab.com"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8082", description = "Document Service"),
        @Server(url = "http://localhost:8080", description = "API Gateway")
    }
)
public class DocumentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DocumentServiceApplication.class, args);
    }
}
