package com.collab.documentservice.controller;

import com.collab.documentservice.dto.DocumentMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketDocumentController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle document content changes from clients
     * Endpoint: /app/document/{documentId}/edit
     * Broadcasts to: /topic/document/{documentId}
     */
    @MessageMapping("/document/{documentId}/edit")
    public void handleDocumentEdit(
            @DestinationVariable String documentId,
            @Payload DocumentMessage message) {
        
        log.info("WebSocket message received - document: {}, type: {}, user: {}", 
            documentId, message.getType(), message.getUserId());

        // Broadcast to all subscribers of this document
        String destination = "/topic/document/" + documentId;
        messagingTemplate.convertAndSend(destination, message);
        
        log.debug("Message broadcasted to: {}", destination);
    }

    /**
     * Handle user joining document editing session
     * Endpoint: /app/document/{documentId}/join
     * Broadcasts to: /topic/document/{documentId}
     */
    @MessageMapping("/document/{documentId}/join")
    public void handleUserJoin(
            @DestinationVariable String documentId,
            @Payload DocumentMessage message) {
        
        log.info("User joined - document: {}, user: {} ({})", 
            documentId, message.getUserId(), message.getUsername());

        String destination = "/topic/document/" + documentId;
        DocumentMessage joinMessage = DocumentMessage.userJoined(
            documentId,
            message.getUserId(),
            message.getUsername()
        );
        messagingTemplate.convertAndSend(destination, joinMessage);
    }

    /**
     * Handle user leaving document editing session
     * Endpoint: /app/document/{documentId}/leave
     * Broadcasts to: /topic/document/{documentId}
     */
    @MessageMapping("/document/{documentId}/leave")
    public void handleUserLeave(
            @DestinationVariable String documentId,
            @Payload DocumentMessage message) {
        
        log.info("User left - document: {}, user: {} ({})", 
            documentId, message.getUserId(), message.getUsername());

        String destination = "/topic/document/" + documentId;
        DocumentMessage leaveMessage = DocumentMessage.userLeft(
            documentId,
            message.getUserId(),
            message.getUsername()
        );
        messagingTemplate.convertAndSend(destination, leaveMessage);
    }
}
