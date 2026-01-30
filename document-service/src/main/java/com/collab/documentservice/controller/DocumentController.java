package com.collab.documentservice.controller;

import com.collab.common.dto.ApiResponse;
import com.collab.documentservice.dto.*;
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
}
