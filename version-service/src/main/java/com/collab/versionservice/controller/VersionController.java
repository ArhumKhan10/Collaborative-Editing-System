package com.collab.versionservice.controller;

import com.collab.common.dto.ApiResponse;
import com.collab.versionservice.dto.ContributionDTO;
import com.collab.versionservice.dto.CreateVersionRequest;
import com.collab.versionservice.dto.VersionDTO;
import com.collab.versionservice.service.VersionService;
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
 * Version Controller
 * 
 * REST API endpoints for document version control and contribution tracking.
 * Manages version snapshots, history retrieval, document reversion, and user contributions.
 * Supports comprehensive version comparison and detailed change analytics.
 * 
 * @author Arhum Khan
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/versions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Version Control", description = "APIs for document version history and contributions")
public class VersionController {

    private final VersionService versionService;

    @PostMapping("/{documentId}")
    @Operation(summary = "Create version snapshot", description = "Create a new version snapshot of the document")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Version created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<ApiResponse<VersionDTO>> createVersion(
            @PathVariable String documentId,
            @Valid @RequestBody CreateVersionRequest request) {
        log.info("POST /api/versions/{}", documentId);
        VersionDTO version = versionService.createVersion(documentId, request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Version created successfully", version));
    }

    @GetMapping("/{documentId}")
    @Operation(summary = "Get version history", description = "Get all versions for a document (sorted by timestamp desc)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Version history retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<VersionDTO>>> getVersionHistory(@PathVariable String documentId) {
        log.info("GET /api/versions/{}", documentId);
        List<VersionDTO> versions = versionService.getVersionHistory(documentId);
        return ResponseEntity.ok(ApiResponse.success(versions));
    }

    @GetMapping("/version/{versionId}")
    @Operation(summary = "Get specific version", description = "Get details of a specific version")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Version retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Version not found")
    })
    public ResponseEntity<ApiResponse<VersionDTO>> getVersion(@PathVariable String versionId) {
        log.info("GET /api/versions/version/{}", versionId);
        VersionDTO version = versionService.getVersion(versionId);
        return ResponseEntity.ok(ApiResponse.success(version));
    }

    @PostMapping("/{documentId}/revert/{versionId}")
    @Operation(summary = "Revert to version", description = "Revert document to a previous version")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document reverted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Version not found")
    })
    public ResponseEntity<ApiResponse<VersionDTO>> revertToVersion(
            @PathVariable String documentId,
            @PathVariable String versionId,
            @Parameter(description = "User ID performing the revert") @RequestParam String userId) {
        log.info("POST /api/versions/{}/revert/{} - userId: {}", documentId, versionId, userId);
        VersionDTO version = versionService.revertToVersion(documentId, versionId, userId);
        return ResponseEntity.ok(ApiResponse.success("Document reverted successfully", version));
    }

    @GetMapping("/{documentId}/contributions")
    @Operation(summary = "Get contributions", description = "Get user contributions for a document")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Contributions retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<ContributionDTO>>> getContributions(@PathVariable String documentId) {
        log.info("GET /api/versions/{}/contributions", documentId);
        List<ContributionDTO> contributions = versionService.getContributions(documentId);
        return ResponseEntity.ok(ApiResponse.success(contributions));
    }
}
