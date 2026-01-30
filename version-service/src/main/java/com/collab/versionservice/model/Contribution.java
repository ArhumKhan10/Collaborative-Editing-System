package com.collab.versionservice.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "contributions")
@CompoundIndex(name = "doc_user_idx", def = "{'documentId': 1, 'userId': 1}")
public class Contribution {

    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;

    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId documentId;

    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId userId;

    private String username;

    private ContributionStats stats;

    public Contribution(ObjectId documentId, ObjectId userId, String username) {
        this.documentId = documentId;
        this.userId = userId;
        this.username = username;
        this.stats = new ContributionStats();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContributionStats {
        private int editsCount = 0;
        private int charsAdded = 0;
        private int charsDeleted = 0;
        private int versionsCreated = 0;
    }
}
