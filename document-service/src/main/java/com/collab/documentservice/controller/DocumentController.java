package com.collab.documentservice.controller;

import com.collab.common.dto.ApiResponse;
import com.collab.documentservice.dto.*;
import com.collab.documentservice.model.DocumentInvitation;
import com.collab.documentservice.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Document Controller
 * 
 * REST API endpoints for document management and collaboration.
 * Handles document CRUD operations, sharing, and access control.
 * Enforces permission-based authorization for all document operations.
 * 
 * @author Arhum Khan
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Document Management", description = "APIs for document creation, editing, and sharing")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    @Operation(summary = "Create new document", description = "Create a new document")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Document created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<ApiResponse<DocumentDTO>> createDocument(@Valid @RequestBody CreateDocumentRequest request) {
        log.info("POST /api/documents - title: {}", request.getTitle());
        DocumentDTO document = documentService.createDocument(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Document created successfully", document));
    }

    @GetMapping("/{documentId}")
    @Operation(summary = "Get document", description = "Retrieve document by ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<ApiResponse<DocumentDTO>> getDocument(
            @PathVariable String documentId,
            @Parameter(description = "User ID making the request") @RequestParam String userId) {
        log.info("GET /api/documents/{} - userId: {}", documentId, userId);
        DocumentDTO document = documentService.getDocument(documentId, userId);
        return ResponseEntity.ok(ApiResponse.success(document));
    }

    @PutMapping("/{documentId}")
    @Operation(summary = "Update document", description = "Update document content and title")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<DocumentDTO>> updateDocument(
            @PathVariable String documentId,
            @Valid @RequestBody UpdateDocumentRequest request,
            @Parameter(description = "User ID making the request") @RequestParam String userId) {
        log.info("PUT /api/documents/{} - userId: {}", documentId, userId);
        DocumentDTO document = documentService.updateDocument(documentId, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Document updated successfully", document));
    }

    @PostMapping("/{documentId}/share")
    @Operation(summary = "Share document", description = "Share document with another user")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document shared successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Only owner can share")
    })
    public ResponseEntity<ApiResponse<String>> shareDocument(
            @PathVariable String documentId,
            @Valid @RequestBody ShareDocumentRequest request,
            @Parameter(description = "Owner user ID") @RequestParam String ownerId) {
        log.info("POST /api/documents/{}/share - with user: {}", documentId, request.getUserId());
        documentService.shareDocument(documentId, request, ownerId);
        return ResponseEntity.ok(ApiResponse.success("Document shared successfully", null));
    }

    @DeleteMapping("/{documentId}")
    @Operation(summary = "Delete document", description = "Delete document (owner only)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Only owner can delete")
    })
    public ResponseEntity<ApiResponse<String>> deleteDocument(
            @PathVariable String documentId,
            @Parameter(description = "Owner user ID") @RequestParam String userId) {
        log.info("DELETE /api/documents/{} - userId: {}", documentId, userId);
        documentService.deleteDocument(documentId, userId);
        return ResponseEntity.ok(ApiResponse.success("Document deleted successfully", null));
    }

    @GetMapping
    @Operation(summary = "Get all accessible documents", description = "Get all documents user owns or has access to")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Documents retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<DocumentDTO>>> getAllAccessibleDocuments(
            @Parameter(description = "User ID") @RequestParam String userId) {
        log.info("GET /api/documents - userId: {}", userId);
        List<DocumentDTO> documents = documentService.getAllAccessibleDocuments(userId);
        return ResponseEntity.ok(ApiResponse.success(documents));
    }

    // ============ INVITATION ENDPOINTS ============

    @PostMapping("/{documentId}/invite")
    @Operation(
        summary = "Send document invitation", 
        description = "Send a collaboration invitation to a user by email. Creates a pending invitation that must be accepted before the user gains access. Only document owners can send invitations."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invitation sent successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "User already has access or pending invitation exists"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User is not the document owner"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found")
    })
    public ResponseEntity<ApiResponse> sendInvitation(
            @Parameter(description = "Document ID") @PathVariable String documentId,
            @Valid @RequestBody InviteRequest request,
            @Parameter(description = "Owner User ID") @RequestParam String ownerId) {
        log.info("POST /api/documents/{}/invite - email: {}, permission: {}", documentId, request.getEmail(), request.getPermission());
        var invitation = documentService.sendInvitation(documentId, ownerId, request.getEmail(), request.getPermission());
        return ResponseEntity.ok(ApiResponse.success("Invitation sent successfully", invitation));
    }

    @GetMapping("/invitations/pending")
    @Operation(
        summary = "Get pending invitations", 
        description = "Retrieve all pending collaboration invitations for a user by email address. Returns invitations awaiting acceptance or declination."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invitations retrieved successfully, returns list of pending invitations")
    })
    public ResponseEntity<ApiResponse> getPendingInvitations(
            @Parameter(description = "User Email") @RequestParam String userEmail) {
        log.info("GET /api/documents/invitations/pending - userEmail: {}", userEmail);
        var invitations = documentService.getPendingInvitations(userEmail);
        return ResponseEntity.ok(ApiResponse.success("Invitations retrieved successfully", invitations));
    }

    @PostMapping("/invitations/{invitationId}/accept")
    @Operation(
        summary = "Accept invitation", 
        description = "Accept a document collaboration invitation. Grants the user access to the document with the specified permission level. Updates invitation status to ACCEPTED."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invitation accepted successfully, returns document details"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invitation expired or already processed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invitation doesn't belong to this user"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Invitation or document not found")
    })
    public ResponseEntity<ApiResponse<DocumentDTO>> acceptInvitation(
            @Parameter(description = "Invitation ID") @PathVariable String invitationId,
            @Parameter(description = "User Email") @RequestParam String userEmail,
            @Parameter(description = "User ID") @RequestParam String userId) {
        log.info("POST /api/documents/invitations/{}/accept - userEmail: {}, userId: {}", invitationId, userEmail, userId);
        DocumentDTO document = documentService.acceptInvitation(invitationId, userEmail, userId);
        return ResponseEntity.ok(ApiResponse.success("Invitation accepted", document));
    }

    @PostMapping("/invitations/{invitationId}/decline")
    @Operation(
        summary = "Decline invitation", 
        description = "Decline a document collaboration invitation. The user will not gain access to the document. Updates invitation status to DECLINED."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invitation declined successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invitation already processed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invitation doesn't belong to this user"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Invitation not found")
    })
    public ResponseEntity<ApiResponse> declineInvitation(
            @Parameter(description = "Invitation ID") @PathVariable String invitationId,
            @Parameter(description = "User Email") @RequestParam String userEmail) {
        log.info("POST /api/documents/invitations/{}/decline - userEmail: {}", invitationId, userEmail);
        documentService.declineInvitation(invitationId, userEmail);
        return ResponseEntity.ok(ApiResponse.success("Invitation declined", null));
    }

    @DeleteMapping("/invitations/{invitationId}")
    @Operation(
        summary = "Cancel invitation", 
        description = "Cancel a sent invitation before it's accepted. Only the invitation sender can perform this action. Updates invitation status to CANCELLED."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invitation cancelled successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User is not the invitation sender"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Invitation not found")
    })
    public ResponseEntity<ApiResponse> cancelInvitation(
            @Parameter(description = "Invitation ID") @PathVariable String invitationId,
            @Parameter(description = "Owner User ID") @RequestParam String ownerId) {
        log.info("DELETE /api/documents/invitations/{} - ownerId: {}", invitationId, ownerId);
        documentService.cancelInvitation(invitationId, ownerId);
        return ResponseEntity.ok(ApiResponse.success("Invitation cancelled", null));
    }

    @GetMapping("/invitations/count")
    @Operation(
        summary = "Get invitation count", 
        description = "Get the count of pending invitations for a user by email address. Used for displaying notification badges."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Count retrieved successfully, returns number of pending invitations")
    })
    public ResponseEntity<ApiResponse<Long>> getInvitationCount(
            @Parameter(description = "User Email") @RequestParam String userEmail) {
        log.info("GET /api/documents/invitations/count - userEmail: {}", userEmail);
        long count = documentService.getInvitationCount(userEmail);
        return ResponseEntity.ok(ApiResponse.success("Invitation count retrieved", count));
    }
}
