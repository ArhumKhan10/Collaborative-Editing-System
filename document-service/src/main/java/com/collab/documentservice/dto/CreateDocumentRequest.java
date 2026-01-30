package com.collab.documentservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create document request")
public class CreateDocumentRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    @Schema(description = "Document title", example = "My Project Proposal")
    private String title;

    @Schema(description = "Initial content (optional)", example = "Start writing here...")
    private String content;

    @NotBlank(message = "Owner ID is required")
    @Schema(description = "Document owner user ID")
    private String ownerId;
}
