package com.collab.versionservice.service;

import com.collab.common.exception.ResourceNotFoundException;
import com.collab.versionservice.dto.ContributionDTO;
import com.collab.versionservice.dto.CreateVersionRequest;
import com.collab.versionservice.dto.VersionDTO;
import com.collab.versionservice.model.Contribution;
import com.collab.versionservice.model.Version;
import com.collab.versionservice.repository.ContributionRepository;
import com.collab.versionservice.repository.VersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Version Service
 * 
 * Manages document version control and contribution tracking.
 * Creates version snapshots, maintains version history, and supports document reversion.
 * Tracks user contributions with detailed change statistics.
 * 
 * @author Arhum Khan
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VersionService {

    private final VersionRepository versionRepository;
    private final ContributionRepository contributionRepository;

    /**
     * Create a new version snapshot
     */
    public VersionDTO createVersion(String documentId, CreateVersionRequest request) {
        log.info("Creating version for document: {}", documentId);

        Version version = new Version(
            new ObjectId(documentId),
            request.getContent(),
            new ObjectId(request.getUserId()),
            request.getDescription()
        );

        // Calculate change stats if there's a previous version
        calculateChangeStats(version);

        Version savedVersion = versionRepository.save(version);
        
        // Update user contributions
        updateContributions(new ObjectId(documentId), new ObjectId(request.getUserId()), version);

        log.info("Version created successfully: {}", savedVersion.getId());
        return VersionDTO.fromVersion(savedVersion);
    }

    /**
     * Get version history for a document
     */
    public List<VersionDTO> getVersionHistory(String documentId) {
        log.info("Fetching version history for document: {}", documentId);

        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");
        List<Version> versions = versionRepository.findByDocumentId(new ObjectId(documentId), sort);

        return versions.stream()
            .map(VersionDTO::fromVersion)
            .collect(Collectors.toList());
    }

    /**
     * Get specific version by ID
     */
    public VersionDTO getVersion(String versionId) {
        log.info("Fetching version: {}", versionId);

        Version version = versionRepository.findById(new ObjectId(versionId))
            .orElseThrow(() -> new ResourceNotFoundException("Version", "id", versionId));

        return VersionDTO.fromVersion(version);
    }

    /**
     * Revert document to a previous version
     */
    public VersionDTO revertToVersion(String documentId, String versionId, String userId) {
        log.info("Reverting document: {} to version: {}", documentId, versionId);

        Version oldVersion = versionRepository.findById(new ObjectId(versionId))
            .orElseThrow(() -> new ResourceNotFoundException("Version", "id", versionId));

        // Create new version with reverted content
        Version newVersion = new Version(
            new ObjectId(documentId),
            oldVersion.getContent(),
            new ObjectId(userId),
            "Reverted to version from " + oldVersion.getTimestamp()
        );

        Version savedVersion = versionRepository.save(newVersion);
        
        // Update contributions
        updateContributions(new ObjectId(documentId), new ObjectId(userId), newVersion);

        log.info("Document reverted successfully, new version: {}", savedVersion.getId());
        return VersionDTO.fromVersion(savedVersion);
    }

    /**
     * Get user contributions for a document
     */
    public List<ContributionDTO> getContributions(String documentId) {
        log.info("Fetching contributions for document: {}", documentId);

        List<Contribution> contributions = contributionRepository.findByDocumentId(new ObjectId(documentId));

        return contributions.stream()
            .map(ContributionDTO::fromContribution)
            .collect(Collectors.toList());
    }

    /**
     * Calculate change statistics
     */
    private void calculateChangeStats(Version version) {
        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");
        List<Version> previousVersions = versionRepository.findByDocumentId(version.getDocumentId(), sort);

        if (!previousVersions.isEmpty()) {
            Version lastVersion = previousVersions.get(0);
            int oldLength = lastVersion.getContent() != null ? lastVersion.getContent().length() : 0;
            int newLength = version.getContent() != null ? version.getContent().length() : 0;

            Version.ChangeStats stats = version.getChangeStats();
            if (newLength > oldLength) {
                stats.setCharsAdded(newLength - oldLength);
            } else if (newLength < oldLength) {
                stats.setCharsDeleted(oldLength - newLength);
            }
            stats.setTotalChanges(Math.abs(newLength - oldLength));
        }
    }

    /**
     * Update user contribution statistics
     */
    private void updateContributions(ObjectId documentId, ObjectId userId, Version version) {
        Contribution contribution = contributionRepository
            .findByDocumentIdAndUserId(documentId, userId)
            .orElse(new Contribution(documentId, userId, "Unknown"));

        Contribution.ContributionStats stats = contribution.getStats();
        stats.setEditsCount(stats.getEditsCount() + 1);
        stats.setVersionsCreated(stats.getVersionsCreated() + 1);
        stats.setCharsAdded(stats.getCharsAdded() + version.getChangeStats().getCharsAdded());
        stats.setCharsDeleted(stats.getCharsDeleted() + version.getChangeStats().getCharsDeleted());

        contributionRepository.save(contribution);
        log.debug("Updated contributions for user: {}", userId);
    }
}
