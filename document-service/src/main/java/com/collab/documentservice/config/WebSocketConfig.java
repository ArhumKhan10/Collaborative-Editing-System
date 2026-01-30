package com.collab.documentservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Configuration
 * 
 * Configures WebSocket support for real-time document collaboration.
 * Sets up STOMP messaging protocol with SockJS fallback support.
 * Defines message broker and application destination prefixes.
 * 
 * @author Arhum Khan
 * @version 1.0.0
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple message broker for /topic destination
        config.enableSimpleBroker("/topic");
        
        // Set application destination prefix
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register STOMP endpoint at /ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // Allow all origins for development
                .withSockJS();  // Enable SockJS fallback
    }
}
