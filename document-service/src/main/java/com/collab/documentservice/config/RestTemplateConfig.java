package com.collab.documentservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * REST Template Configuration
 * 
 * Configures RestTemplate bean for making HTTP calls to other microservices.
 * Used for inter-service communication (e.g., fetching user details from User Service).
 * 
 * @author Arhum Khan
 * @version 1.0.0
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Create RestTemplate bean for HTTP communication
     * 
     * @return Configured RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
