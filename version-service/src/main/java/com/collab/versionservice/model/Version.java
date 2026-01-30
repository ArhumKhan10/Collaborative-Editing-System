package com.collab.versionservice.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Version Entity
 * 
 * Represents a snapshot of a document at a specific point in time.
 * Stores complete document content, user information, and change statistics.
 * Used for version history, reversion, and contribution tracking.
 * 
 * @author Arhum Khan
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "versions")
public class Version {

    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;

    @Indexed
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId documentId;

    private String content;

    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId userId;

    @Indexed
    private LocalDateTime timestamp;

    private String description;

    private ChangeStats changeStats;

    public Version(ObjectId documentId, String content, ObjectId userId, String description) {
        this.documentId = documentId;
        this.content = content;
        this.userId = userId;
        this.description = description;
        this.timestamp = LocalDateTime.now();
        this.changeStats = new ChangeStats();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeStats {
        private int charsAdded = 0;
        private int charsDeleted = 0;
        private int totalChanges = 0;
    }
}
