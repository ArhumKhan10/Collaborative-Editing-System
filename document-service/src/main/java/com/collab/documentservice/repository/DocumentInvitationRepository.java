package com.collab.documentservice.repository;

import com.collab.documentservice.model.DocumentInvitation;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Document Invitation operations
 * 
 * @author Arhum Khan
 * @version 1.0.0
 */
@Repository
public interface DocumentInvitationRepository extends MongoRepository<DocumentInvitation, ObjectId> {

    /**
     * Find all pending invitations for a user
     */
    @Query("{'invitedUser.userId': ?0, 'status': 'PENDING'}")
    List<DocumentInvitation> findPendingInvitationsByUserId(String userId);

    /**
     * Find all invitations sent by a user
     */
    @Query("{'invitedBy.userId': ?0}")
    List<DocumentInvitation> findInvitationsSentByUser(String userId);

    /**
     * Find pending invitation for specific document and user email
     */
    @Query("{'documentId': ?0, 'invitedUser.email': ?1, 'status': 'PENDING'}")
    Optional<DocumentInvitation> findPendingInvitation(ObjectId documentId, String email);

    /**
     * Find all invitations for a document
     */
    List<DocumentInvitation> findByDocumentId(ObjectId documentId);

    /**
     * Count pending invitations for a user
     */
    @Query(value = "{'invitedUser.userId': ?0, 'status': 'PENDING'}", count = true)
    long countPendingInvitationsByUserId(String userId);
}
