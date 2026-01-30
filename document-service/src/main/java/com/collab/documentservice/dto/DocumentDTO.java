package com.collab.documentservice.dto;

import com.collab.documentservice.model.Document;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Document data transfer object")
public class DocumentDTO {

    @Schema(description = "Document ID")
    private String id;

    @Schema(description = "Document title")
    private String title;

    @Schema(description = "Document content")
    private String content;

    @Schema(description = "Owner user ID")
    private String ownerId;

    @Schema(description = "List of collaborators")
    private List<CollaboratorDTO> collaborators;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Last modified by user ID")
    private String lastModifiedBy;

    public static DocumentDTO fromDocument(Document document) {
        List<CollaboratorDTO> collabDTOs = document.getCollaborators().stream()
            .map(c -> new CollaboratorDTO(
                c.getUserId().toString(),
                c.getPermission(),
                c.getAddedAt()
            ))
            .collect(Collectors.toList());

        return new DocumentDTO(
            document.getId().toString(),
            document.getTitle(),
            document.getContent(),
            document.getOwnerId().toString(),
            collabDTOs,
            document.getCreatedAt(),
            document.getUpdatedAt(),
            document.getLastModifiedBy() != null ? document.getLastModifiedBy().toString() : null
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollaboratorDTO {
        private String userId;
        private String permission;
        private LocalDateTime addedAt;
    }
}
