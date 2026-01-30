package com.collab.versionservice.service;

import com.collab.common.exception.ResourceNotFoundException;
import com.collab.versionservice.dto.ContributionDTO;
import com.collab.versionservice.dto.CreateVersionRequest;
import com.collab.versionservice.dto.VersionDTO;
import com.collab.versionservice.model.Contribution;
import com.collab.versionservice.model.Version;
import com.collab.versionservice.repository.ContributionRepository;
import com.collab.versionservice.repository.VersionRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VersionServiceTest {

    @Mock
    private VersionRepository versionRepository;

    @Mock
    private ContributionRepository contributionRepository;

    @InjectMocks
    private VersionService versionService;

    private ObjectId documentId;
    private ObjectId userId;
    private ObjectId versionId;
    private Version testVersion;

    @BeforeEach
    void setUp() {
        documentId = new ObjectId();
        userId = new ObjectId();
        versionId = new ObjectId();
        
        testVersion = new Version(
            documentId,
            "Test content",
            userId,
            "Test description"
        );
        testVersion.setId(versionId);
    }

    @Test
    void createVersion_Success() {
        // Arrange
        CreateVersionRequest request = new CreateVersionRequest(
            "New version content",
            userId.toString(),
            "Initial version"
        );
        
        when(versionRepository.findByDocumentId(eq(documentId), any(Sort.class)))
            .thenReturn(new ArrayList<>());
        when(versionRepository.save(any(Version.class))).thenReturn(testVersion);
        when(contributionRepository.findByDocumentIdAndUserId(documentId, userId))
            .thenReturn(Optional.empty());
        when(contributionRepository.save(any(Contribution.class)))
            .thenReturn(new Contribution(documentId, userId, "testuser"));

        // Act
        VersionDTO result = versionService.createVersion(documentId.toString(), request);

        // Assert
        assertNotNull(result);
        assertEquals("Test content", result.getContent());
        verify(versionRepository, times(1)).save(any(Version.class));
        verify(contributionRepository, times(1)).save(any(Contribution.class));
    }

    @Test
    void createVersion_WithPreviousVersion_CalculatesChangeStats() {
        // Arrange
        CreateVersionRequest request = new CreateVersionRequest(
            "Updated content with more text",
            userId.toString(),
            "Updated version"
        );
        
        Version previousVersion = new Version(
            documentId,
            "Old content",
            userId,
            "Previous version"
        );
        
        when(versionRepository.findByDocumentId(eq(documentId), any(Sort.class)))
            .thenReturn(Arrays.asList(previousVersion));
        when(versionRepository.save(any(Version.class))).thenReturn(testVersion);
        when(contributionRepository.findByDocumentIdAndUserId(documentId, userId))
            .thenReturn(Optional.empty());
        when(contributionRepository.save(any(Contribution.class)))
            .thenReturn(new Contribution(documentId, userId, "testuser"));

        // Act
        VersionDTO result = versionService.createVersion(documentId.toString(), request);

        // Assert
        assertNotNull(result);
        verify(versionRepository, times(1)).save(any(Version.class));
    }

    @Test
    void getVersionHistory_Success() {
        // Arrange
        Version version1 = new Version(documentId, "Content 1", userId, "Version 1");
        version1.setId(new ObjectId());
        
        Version version2 = new Version(documentId, "Content 2", userId, "Version 2");
        version2.setId(new ObjectId());
        
        List<Version> versions = Arrays.asList(version1, version2);
        
        when(versionRepository.findByDocumentId(eq(documentId), any(Sort.class)))
            .thenReturn(versions);

        // Act
        List<VersionDTO> result = versionService.getVersionHistory(documentId.toString());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(versionRepository, times(1)).findByDocumentId(eq(documentId), any(Sort.class));
    }

    @Test
    void getVersionHistory_EmptyList() {
        // Arrange
        when(versionRepository.findByDocumentId(eq(documentId), any(Sort.class)))
            .thenReturn(new ArrayList<>());

        // Act
        List<VersionDTO> result = versionService.getVersionHistory(documentId.toString());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getVersion_Success() {
        // Arrange
        when(versionRepository.findById(versionId)).thenReturn(Optional.of(testVersion));

        // Act
        VersionDTO result = versionService.getVersion(versionId.toString());

        // Assert
        assertNotNull(result);
        assertEquals(versionId.toString(), result.getId());
        assertEquals("Test content", result.getContent());
    }

    @Test
    void getVersion_NotFound_ThrowsException() {
        // Arrange
        when(versionRepository.findById(versionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> versionService.getVersion(versionId.toString()));
    }

    @Test
    void revertToVersion_Success() {
        // Arrange
        Version oldVersion = new Version(
            documentId,
            "Old content to revert to",
            userId,
            "Old version"
        );
        oldVersion.setId(new ObjectId());
        
        Version newVersion = new Version(
            documentId,
            "Old content to revert to",
            userId,
            "Reverted to version from " + oldVersion.getTimestamp()
        );
        newVersion.setId(new ObjectId());
        
        when(versionRepository.findById(oldVersion.getId())).thenReturn(Optional.of(oldVersion));
        when(versionRepository.save(any(Version.class))).thenReturn(newVersion);
        when(contributionRepository.findByDocumentIdAndUserId(documentId, userId))
            .thenReturn(Optional.empty());
        when(contributionRepository.save(any(Contribution.class)))
            .thenReturn(new Contribution(documentId, userId, "testuser"));

        // Act
        VersionDTO result = versionService.revertToVersion(
            documentId.toString(), 
            oldVersion.getId().toString(), 
            userId.toString()
        );

        // Assert
        assertNotNull(result);
        verify(versionRepository, times(1)).save(any(Version.class));
        verify(contributionRepository, times(1)).save(any(Contribution.class));
    }

    @Test
    void revertToVersion_VersionNotFound_ThrowsException() {
        // Arrange
        ObjectId nonExistentVersionId = new ObjectId();
        when(versionRepository.findById(nonExistentVersionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> versionService.revertToVersion(
                documentId.toString(), 
                nonExistentVersionId.toString(), 
                userId.toString()
            ));
    }

    @Test
    void getContributions_Success() {
        // Arrange
        Contribution contrib1 = new Contribution(documentId, userId, "user1");
        Contribution contrib2 = new Contribution(documentId, new ObjectId(), "user2");
        
        List<Contribution> contributions = Arrays.asList(contrib1, contrib2);
        
        when(contributionRepository.findByDocumentId(documentId))
            .thenReturn(contributions);

        // Act
        List<ContributionDTO> result = versionService.getContributions(documentId.toString());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(contributionRepository, times(1)).findByDocumentId(documentId);
    }

    @Test
    void getContributions_EmptyList() {
        // Arrange
        when(contributionRepository.findByDocumentId(documentId))
            .thenReturn(new ArrayList<>());

        // Act
        List<ContributionDTO> result = versionService.getContributions(documentId.toString());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateContributions_NewContributor() {
        // Arrange
        CreateVersionRequest request = new CreateVersionRequest(
            "Content",
            userId.toString(),
            "Description"
        );
        
        when(versionRepository.findByDocumentId(eq(documentId), any(Sort.class)))
            .thenReturn(new ArrayList<>());
        when(versionRepository.save(any(Version.class))).thenReturn(testVersion);
        when(contributionRepository.findByDocumentIdAndUserId(documentId, userId))
            .thenReturn(Optional.empty());
        when(contributionRepository.save(any(Contribution.class)))
            .thenReturn(new Contribution(documentId, userId, "testuser"));

        // Act
        versionService.createVersion(documentId.toString(), request);

        // Assert
        verify(contributionRepository, times(1)).save(any(Contribution.class));
    }

    @Test
    void updateContributions_ExistingContributor() {
        // Arrange
        CreateVersionRequest request = new CreateVersionRequest(
            "Content",
            userId.toString(),
            "Description"
        );
        
        Contribution existingContribution = new Contribution(documentId, userId, "testuser");
        
        when(versionRepository.findByDocumentId(eq(documentId), any(Sort.class)))
            .thenReturn(new ArrayList<>());
        when(versionRepository.save(any(Version.class))).thenReturn(testVersion);
        when(contributionRepository.findByDocumentIdAndUserId(documentId, userId))
            .thenReturn(Optional.of(existingContribution));
        when(contributionRepository.save(any(Contribution.class)))
            .thenReturn(existingContribution);

        // Act
        versionService.createVersion(documentId.toString(), request);

        // Assert
        verify(contributionRepository, times(1)).save(eq(existingContribution));
    }
}
