package com.collab.documentservice.service;

import com.collab.common.exception.BadRequestException;
import com.collab.common.exception.ResourceNotFoundException;
import com.collab.common.exception.UnauthorizedException;
import com.collab.documentservice.dto.*;
import com.collab.documentservice.model.Document;
import com.collab.documentservice.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;

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
}
