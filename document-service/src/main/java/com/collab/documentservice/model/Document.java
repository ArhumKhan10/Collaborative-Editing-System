package com.collab.documentservice.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.mongodb.core.mapping.Document(collection = "documents")
public class Document {

    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;

    private String title;

    private String content;

    @Indexed
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId ownerId;

    private List<Collaborator> collaborators = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId lastModifiedBy;

    public Document(String title, String content, ObjectId ownerId) {
        this.title = title;
        this.content = content != null ? content : "";
        this.ownerId = ownerId;
        this.collaborators = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastModifiedBy = ownerId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Collaborator {
        @JsonSerialize(using = ToStringSerializer.class)
        private ObjectId userId;
        private String permission; // "edit" or "view"
        private LocalDateTime addedAt;

        public Collaborator(ObjectId userId, String permission) {
            this.userId = userId;
            this.permission = permission;
            this.addedAt = LocalDateTime.now();
        }
    }
}
