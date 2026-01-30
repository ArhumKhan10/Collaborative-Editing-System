package com.collab.documentservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update document request")
public class UpdateDocumentRequest {

    @Schema(description = "Document content")
    private String content;

    @Schema(description = "Document title")
    private String title;

    @Schema(description = "User ID who made the modification")
    private String lastModifiedBy;
}
