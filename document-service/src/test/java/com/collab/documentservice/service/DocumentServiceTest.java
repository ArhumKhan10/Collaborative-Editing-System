package com.collab.documentservice.service;

import com.collab.common.exception.BadRequestException;
import com.collab.common.exception.ResourceNotFoundException;
import com.collab.common.exception.UnauthorizedException;
import com.collab.documentservice.dto.*;
import com.collab.documentservice.model.Document;
import com.collab.documentservice.repository.DocumentRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private DocumentService documentService;

    private ObjectId ownerId;
    private ObjectId documentId;
    private Document testDocument;

    @BeforeEach
    void setUp() {
        ownerId = new ObjectId();
        documentId = new ObjectId();
        testDocument = new Document("Test Document", "Test content", ownerId);
        testDocument.setId(documentId);
    }

    @Test
    void createDocument_Success() {
        // Arrange
        CreateDocumentRequest request = new CreateDocumentRequest(
            "New Document", 
            "Initial content", 
            ownerId.toString()
        );
        
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);

        // Act
        DocumentDTO result = documentService.createDocument(request);

        // Assert
        assertNotNull(result);
        assertEquals("Test Document", result.getTitle());
        assertEquals("Test content", result.getContent());
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void getDocument_AsOwner_Success() {
        // Arrange
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(testDocument));

        // Act
        DocumentDTO result = documentService.getDocument(documentId.toString(), ownerId.toString());

        // Assert
        assertNotNull(result);
        assertEquals("Test Document", result.getTitle());
        assertEquals(ownerId.toString(), result.getOwnerId());
    }

    @Test
    void getDocument_NotFound_ThrowsException() {
        // Arrange
        when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> documentService.getDocument(documentId.toString(), ownerId.toString()));
    }

    @Test
    void getDocument_Unauthorized_ThrowsException() {
        // Arrange
        ObjectId unauthorizedUserId = new ObjectId();
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(testDocument));

        // Act & Assert
        assertThrows(UnauthorizedException.class, 
            () -> documentService.getDocument(documentId.toString(), unauthorizedUserId.toString()));
    }

    @Test
    void updateDocument_AsOwner_Success() {
        // Arrange
        UpdateDocumentRequest request = new UpdateDocumentRequest(
            "Updated Title",
            "Updated content",
            ownerId.toString()
        );
        
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(testDocument));
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);

        // Act
        DocumentDTO result = documentService.updateDocument(
            documentId.toString(), 
            request, 
            ownerId.toString()
        );

        // Assert
        assertNotNull(result);
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void updateDocument_Unauthorized_ThrowsException() {
        // Arrange
        ObjectId unauthorizedUserId = new ObjectId();
        UpdateDocumentRequest request = new UpdateDocumentRequest(
            "Updated Title",
            "Updated content",
            unauthorizedUserId.toString()
        );
        
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(testDocument));

        // Act & Assert
        assertThrows(UnauthorizedException.class, 
            () -> documentService.updateDocument(
                documentId.toString(), 
                request, 
                unauthorizedUserId.toString()
            ));
    }

    @Test
    void shareDocument_AsOwner_Success() {
        // Arrange
        ObjectId collaboratorId = new ObjectId();
        ShareDocumentRequest request = new ShareDocumentRequest(
            collaboratorId.toString(),
            "edit"
        );
        
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(testDocument));
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);

        // Act
        documentService.shareDocument(documentId.toString(), request, ownerId.toString());

        // Assert
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void shareDocument_NotOwner_ThrowsException() {
        // Arrange
        ObjectId nonOwnerId = new ObjectId();
        ObjectId collaboratorId = new ObjectId();
        ShareDocumentRequest request = new ShareDocumentRequest(
            collaboratorId.toString(),
            "edit"
        );
        
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(testDocument));

        // Act & Assert
        assertThrows(UnauthorizedException.class, 
            () -> documentService.shareDocument(
                documentId.toString(), 
                request, 
                nonOwnerId.toString()
            ));
    }

    @Test
    void shareDocument_AlreadyCollaborator_ThrowsException() {
        // Arrange
        ObjectId collaboratorId = new ObjectId();
        
        // Add collaborator to document
        Document.Collaborator existingCollab = new Document.Collaborator(collaboratorId, "view");
        testDocument.getCollaborators().add(existingCollab);
        
        ShareDocumentRequest request = new ShareDocumentRequest(
            collaboratorId.toString(),
            "edit"
        );
        
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(testDocument));

        // Act & Assert
        assertThrows(BadRequestException.class, 
            () -> documentService.shareDocument(
                documentId.toString(), 
                request, 
                ownerId.toString()
            ));
    }

    @Test
    void deleteDocument_AsOwner_Success() {
        // Arrange
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(testDocument));

        // Act
        documentService.deleteDocument(documentId.toString(), ownerId.toString());

        // Assert
        verify(documentRepository, times(1)).delete(testDocument);
    }

    @Test
    void deleteDocument_NotOwner_ThrowsException() {
        // Arrange
        ObjectId nonOwnerId = new ObjectId();
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(testDocument));

        // Act & Assert
        assertThrows(UnauthorizedException.class, 
            () -> documentService.deleteDocument(documentId.toString(), nonOwnerId.toString()));
        verify(documentRepository, never()).delete(any(Document.class));
    }

    @Test
    void getAllAccessibleDocuments_OwnerAndCollaborator() {
        // Arrange
        ObjectId userId = new ObjectId();
        
        Document ownedDoc = new Document("Owned Doc", "Content", userId);
        ownedDoc.setId(new ObjectId());
        
        Document sharedDoc = new Document("Shared Doc", "Content", ownerId);
        sharedDoc.setId(new ObjectId());
        Document.Collaborator collab = new Document.Collaborator(userId, "edit");
        sharedDoc.getCollaborators().add(collab);
        
        List<Document> documents = Arrays.asList(ownedDoc, sharedDoc);
        
        when(documentRepository.findAllAccessibleByUserId(userId)).thenReturn(documents);

        // Act
        List<DocumentDTO> result = documentService.getAllAccessibleDocuments(userId.toString());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(documentRepository, times(1)).findAllAccessibleByUserId(userId);
    }

    @Test
    void getAllAccessibleDocuments_EmptyList() {
        // Arrange
        ObjectId userId = new ObjectId();
        when(documentRepository.findAllAccessibleByUserId(userId)).thenReturn(new ArrayList<>());

        // Act
        List<DocumentDTO> result = documentService.getAllAccessibleDocuments(userId.toString());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
