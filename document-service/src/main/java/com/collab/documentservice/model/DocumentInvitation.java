package com.collab.documentservice.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Document Invitation Entity
 * 
 * Represents an invitation to collaborate on a document.
 * Supports handshake approval workflow where invited users
 * must accept or decline before gaining access.
 * 
 * @author Arhum Khan
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "document_invitations")
public class DocumentInvitation {

    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;

    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId documentId;

    private String documentTitle;

    private InvitationUser invitedBy;

    private InvitationUser invitedUser;

    private String permission; // edit or view

    private String status; // PENDING, ACCEPTED, DECLINED, CANCELLED, EXPIRED

    private String message;

    private LocalDateTime invitedAt;

    private LocalDateTime respondedAt;

    private LocalDateTime expiresAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvitationUser {
        private String userId;
        private String username;
        private String email;

        public InvitationUser(String userId, String email) {
            this.userId = userId;
            this.email = email;
        }
    }
}
