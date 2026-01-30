package com.collab.documentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * WebSocket message for real-time document collaboration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentMessage {

    private String type; // "content-change", "user-joined", "user-left", "cursor-position"
    private String documentId;
    private String userId;
    private String username;
    private String content;
    private Integer cursorPosition;
    private LocalDateTime timestamp;

    public static DocumentMessage contentChange(String documentId, String userId, String username, String content) {
        return new DocumentMessage(
            "content-change",
            documentId,
            userId,
            username,
            content,
            null,
            LocalDateTime.now()
        );
    }

    public static DocumentMessage userJoined(String documentId, String userId, String username) {
        return new DocumentMessage(
            "user-joined",
            documentId,
            userId,
            username,
            null,
            null,
            LocalDateTime.now()
        );
    }

    public static DocumentMessage userLeft(String documentId, String userId, String username) {
        return new DocumentMessage(
            "user-left",
            documentId,
            userId,
            username,
            null,
            null,
            LocalDateTime.now()
        );
    }
}
