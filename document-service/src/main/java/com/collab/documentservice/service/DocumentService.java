package com.collab.documentservice.service;

import com.collab.common.exception.BadRequestException;
import com.collab.common.exception.ResourceNotFoundException;
import com.collab.common.exception.UnauthorizedException;
import com.collab.documentservice.dto.*;
import com.collab.documentservice.model.Document;
import com.collab.documentservice.model.DocumentInvitation;
import com.collab.documentservice.repository.DocumentRepository;
import com.collab.documentservice.repository.DocumentInvitationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Document Service
 * 
 * Core business logic for document management and collaboration.
 * Handles document CRUD operations, sharing, permissions, and access control.
 * Supports role-based permissions (owner, edit, view).
 * 
 * @author Arhum Khan
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentInvitationRepository invitationRepository;
    private final RestTemplate restTemplate;
    
    @Value("${api.gateway.url:http://localhost:8080}")
    private String apiGatewayUrl;

    /**
     * Create a new document
     */
    public DocumentDTO createDocument(CreateDocumentRequest request) {
        log.info("Creating new document: {}", request.getTitle());

        Document document = new Document(
            request.getTitle(),
            request.getContent(),
            new ObjectId(request.getOwnerId())
        );

        Document savedDocument = documentRepository.save(document);
        log.info("Document created successfully: {}", savedDocument.getId());

        return DocumentDTO.fromDocument(savedDocument);
    }

    /**
     * Get document by ID (with permission check)
     */
    public DocumentDTO getDocument(String documentId, String userId) {
        log.info("Fetching document: {} for user: {}", documentId, userId);

        Document document = documentRepository.findById(new ObjectId(documentId))
            .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));

        // Check if user has access
        if (!hasAccess(document, new ObjectId(userId))) {
            throw new UnauthorizedException("You don't have permission to access this document");
        }

        return DocumentDTO.fromDocument(document);
    }

    /**
     * Update document content
     */
    public DocumentDTO updateDocument(String documentId, UpdateDocumentRequest request, String userId) {
        log.info("Updating document: {} by user: {}", documentId, userId);

        Document document = documentRepository.findById(new ObjectId(documentId))
            .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));

        // Check if user has edit permission
        if (!hasEditPermission(document, new ObjectId(userId))) {
            throw new UnauthorizedException("You don't have permission to edit this document");
        }

        // Update fields
        if (request.getTitle() != null) {
            document.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            document.setContent(request.getContent());
        }
        if (request.getLastModifiedBy() != null) {
            document.setLastModifiedBy(new ObjectId(request.getLastModifiedBy()));
        }

        Document updatedDocument = documentRepository.save(document);
        log.info("Document updated successfully: {}", documentId);

        return DocumentDTO.fromDocument(updatedDocument);
    }

    /**
     * Share document with another user
     */
    public void shareDocument(String documentId, ShareDocumentRequest request, String requesterId) {
        log.info("Sharing document: {} with user: {}", documentId, request.getUserId());

        Document document = documentRepository.findById(new ObjectId(documentId))
            .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));

        // Check if requester has permission to share (owner or collaborator with edit permission)
        ObjectId requesterObjectId = new ObjectId(requesterId);
        boolean isOwner = document.getOwnerId().equals(requesterObjectId);
        boolean hasEditPermission = document.getCollaborators().stream()
            .anyMatch(c -> c.getUserId().equals(requesterObjectId) && "edit".equals(c.getPermission()));
        
        if (!isOwner && !hasEditPermission) {
            throw new UnauthorizedException("Only document owner or editors can share");
        }

        ObjectId newCollaboratorId = new ObjectId(request.getUserId());

        // Check if already a collaborator
        boolean alreadyCollaborator = document.getCollaborators().stream()
            .anyMatch(c -> c.getUserId().equals(newCollaboratorId));

        if (alreadyCollaborator) {
            throw new BadRequestException("User is already a collaborator");
        }

        // Add new collaborator
        Document.Collaborator collaborator = new Document.Collaborator(
            newCollaboratorId,
            request.getPermission()
        );
        document.getCollaborators().add(collaborator);

        documentRepository.save(document);
        log.info("Document shared successfully with user: {}", request.getUserId());
    }

    /**
     * Delete document
     */
    public void deleteDocument(String documentId, String userId) {
        log.info("Deleting document: {} by user: {}", documentId, userId);

        Document document = documentRepository.findById(new ObjectId(documentId))
            .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));

        // Only owner can delete
        if (!document.getOwnerId().equals(new ObjectId(userId))) {
            throw new UnauthorizedException("Only document owner can delete");
        }

        documentRepository.delete(document);
        log.info("Document deleted successfully: {}", documentId);
    }

    /**
     * Get all documents accessible by user
     */
    public List<DocumentDTO> getAllAccessibleDocuments(String userId) {
        log.info("Fetching all documents for user: {}", userId);

        List<Document> documents = documentRepository.findAllAccessibleByUserId(new ObjectId(userId));

        return documents.stream()
            .map(DocumentDTO::fromDocument)
            .collect(Collectors.toList());
    }

    /**
     * Check if user has access to document (owner or collaborator)
     */
    private boolean hasAccess(Document document, ObjectId userId) {
        if (document.getOwnerId().equals(userId)) {
            return true;
        }

        return document.getCollaborators().stream()
            .anyMatch(c -> c.getUserId().equals(userId));
    }

    /**
     * Check if user has edit permission
     */
    private boolean hasEditPermission(Document document, ObjectId userId) {
        if (document.getOwnerId().equals(userId)) {
            return true;
        }

        return document.getCollaborators().stream()
            .anyMatch(c -> c.getUserId().equals(userId) && "edit".equals(c.getPermission()));
    }

    /**
     * Send document invitation to a user
     * 
     * Creates a pending invitation for the specified email address to collaborate on a document.
     * Only the document owner can send invitations. Validates that the user doesn't already have
     * access and no pending invitation exists.
     * 
     * @param documentId The ID of the document to share
     * @param invitedByUserId The ID of the user sending the invitation (must be document owner)
     * @param invitedEmail The email address of the user being invited
     * @param permission The permission level ('edit' or 'view')
     * @return The created invitation entity
     * @throws ResourceNotFoundException if document not found
     * @throws UnauthorizedException if sender is not the document owner
     * @throws BadRequestException if user already has access or pending invitation exists
     */
    public DocumentInvitation sendInvitation(String documentId, String invitedByUserId, String invitedEmail, String permission) {
        log.info("Sending invitation for document {} to {}", documentId, invitedEmail);

        Document document = documentRepository.findById(new ObjectId(documentId))
            .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        if (!document.getOwnerId().toString().equals(invitedByUserId)) {
            throw new UnauthorizedException("Only document owner can send invitations");
        }

        // Check if user already has access
        boolean alreadyHasAccess = document.getCollaborators().stream()
            .anyMatch(c -> c.getEmail() != null && c.getEmail().equals(invitedEmail));

        if (alreadyHasAccess) {
            throw new BadRequestException("User already has access to this document");
        }

        // Check if pending invitation already exists
        Optional<DocumentInvitation> existingInvitation = invitationRepository
            .findPendingInvitation(new ObjectId(documentId), invitedEmail);

        if (existingInvitation.isPresent()) {
            throw new BadRequestException("Invitation already sent to this user");
        }

        DocumentInvitation invitation = new DocumentInvitation();
        invitation.setDocumentId(new ObjectId(documentId));
        invitation.setDocumentTitle(document.getTitle());
        
        // We need to get the owner's email to show who sent the invitation
        // For now, we'll fetch it via HTTP call to user-service
        String ownerEmail = getUserEmail(invitedByUserId);
        
        DocumentInvitation.InvitationUser invitedBy = new DocumentInvitation.InvitationUser();
        invitedBy.setUserId(invitedByUserId);
        invitedBy.setEmail(ownerEmail);  // Owner's email (sender)
        invitation.setInvitedBy(invitedBy);

        DocumentInvitation.InvitationUser invitedUser = new DocumentInvitation.InvitationUser();
        invitedUser.setEmail(invitedEmail);  // Recipient's email
        invitation.setInvitedUser(invitedUser);

        invitation.setPermission(permission);
        invitation.setStatus("PENDING");
        invitation.setMessage("You've been invited to collaborate on " + document.getTitle());
        invitation.setInvitedAt(java.time.LocalDateTime.now());
        invitation.setExpiresAt(java.time.LocalDateTime.now().plusDays(7));

        invitation = invitationRepository.save(invitation);
        log.info("Invitation sent successfully: {}", invitation.getId());

        return invitation;
    }

    /**
     * Get all pending invitations for a user
     * 
     * Retrieves all invitations with PENDING status for the specified email address.
     * Used to display invitations in the user's invitation center.
     * 
     * @param userEmail The email address of the invited user
     * @return List of pending invitations for the user
     */
    public List<DocumentInvitation> getPendingInvitations(String userEmail) {
        return invitationRepository.findAll().stream()
            .filter(inv -> "PENDING".equals(inv.getStatus()) && 
                          inv.getInvitedUser().getEmail().equals(userEmail))
            .collect(Collectors.toList());
    }

    /**
     * Accept a document invitation
     * 
     * Validates the invitation belongs to the user, is still pending, and hasn't expired.
     * Adds the user as a collaborator to the document with the specified permission level
     * and updates the invitation status to ACCEPTED.
     * 
     * @param invitationId The ID of the invitation to accept
     * @param userEmail The email address of the user accepting (must match invitation)
     * @param userId The ID of the user accepting
     * @return The document DTO with updated collaborators list
     * @throws ResourceNotFoundException if invitation or document not found
     * @throws UnauthorizedException if invitation doesn't belong to this user
     * @throws BadRequestException if invitation is not pending or has expired
     */
    public DocumentDTO acceptInvitation(String invitationId, String userEmail, String userId) {
        log.info("Accepting invitation {} for user {}", invitationId, userEmail);

        DocumentInvitation invitation = invitationRepository.findById(new ObjectId(invitationId))
            .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (!invitation.getInvitedUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("This invitation is not for you");
        }

        if (!"PENDING".equals(invitation.getStatus())) {
            throw new BadRequestException("Invitation is no longer pending");
        }

        if (invitation.getExpiresAt() != null && 
            java.time.LocalDateTime.now().isAfter(invitation.getExpiresAt())) {
            invitation.setStatus("EXPIRED");
            invitationRepository.save(invitation);
            throw new BadRequestException("Invitation has expired");
        }

        Document document = documentRepository.findById(invitation.getDocumentId())
            .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        // Add user as collaborator with their actual userId
        Document.Collaborator collaborator = new Document.Collaborator();
        collaborator.setUserId(new ObjectId(userId));
        collaborator.setEmail(userEmail);
        collaborator.setPermission(invitation.getPermission());
        document.getCollaborators().add(collaborator);

        documentRepository.save(document);

        // Update invitation status
        invitation.setStatus("ACCEPTED");
        invitation.setRespondedAt(java.time.LocalDateTime.now());
        invitationRepository.save(invitation);

        log.info("Invitation accepted successfully");

        return DocumentDTO.fromDocument(document);
    }

    /**
     * Decline a document invitation
     * 
     * Updates the invitation status to DECLINED. The user will not be granted access
     * to the document. The sender can send a new invitation later if desired.
     * 
     * @param invitationId The ID of the invitation to decline
     * @param userEmail The email address of the user declining (must match invitation)
     * @throws ResourceNotFoundException if invitation not found
     * @throws UnauthorizedException if invitation doesn't belong to this user
     * @throws BadRequestException if invitation is not pending
     */
    public void declineInvitation(String invitationId, String userEmail) {
        log.info("Declining invitation {} for user {}", invitationId, userEmail);

        DocumentInvitation invitation = invitationRepository.findById(new ObjectId(invitationId))
            .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (!invitation.getInvitedUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("This invitation is not for you");
        }

        if (!"PENDING".equals(invitation.getStatus())) {
            throw new BadRequestException("Invitation is no longer pending");
        }

        invitation.setStatus("DECLINED");
        invitation.setRespondedAt(java.time.LocalDateTime.now());
        invitationRepository.save(invitation);

        log.info("Invitation declined");
    }

    /**
     * Cancel a sent invitation
     * 
     * Allows the invitation sender to cancel a pending invitation before it's accepted.
     * Only the user who sent the invitation can cancel it.
     * 
     * @param invitationId The ID of the invitation to cancel
     * @param ownerUserId The ID of the user canceling (must be invitation sender)
     * @throws ResourceNotFoundException if invitation not found
     * @throws UnauthorizedException if user is not the invitation sender
     */
    public void cancelInvitation(String invitationId, String ownerUserId) {
        log.info("Cancelling invitation {} by owner {}", invitationId, ownerUserId);

        DocumentInvitation invitation = invitationRepository.findById(new ObjectId(invitationId))
            .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (!invitation.getInvitedBy().getUserId().equals(ownerUserId)) {
            throw new UnauthorizedException("Only invitation sender can cancel");
        }

        invitation.setStatus("CANCELLED");
        invitationRepository.save(invitation);

        log.info("Invitation cancelled");
    }

    /**
     * Get count of pending invitations for a user
     * 
     * Returns the number of pending invitations for displaying badge notifications.
     * 
     * @param userEmail The email address of the user
     * @return Count of pending invitations
     */
    public long getInvitationCount(String userEmail) {
        return getPendingInvitations(userEmail).size();
    }
    
    /**
     * Get user email from user service via API Gateway
     * 
     * @param userId The user ID
     * @return The user's email address
     */
    private String getUserEmail(String userId) {
        try {
            String url = apiGatewayUrl + "/api/users/profile/" + userId;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.get("data") != null) {
                Map<String, Object> userData = (Map<String, Object>) response.get("data");
                String email = (String) userData.get("email");
                if (email != null && !email.isEmpty()) {
                    return email;
                }
            }
            
            log.warn("Could not fetch email for user {}, using userId as fallback", userId);
            return "User " + userId.substring(0, Math.min(8, userId.length()));
        } catch (Exception e) {
            log.error("Error fetching user email for userId {}: {}", userId, e.getMessage());
            return "User " + userId.substring(0, Math.min(8, userId.length()));
        }
    }
}
