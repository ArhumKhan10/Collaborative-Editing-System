package com.collab.versionservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create version snapshot request")
public class CreateVersionRequest {

    @NotBlank(message = "Content is required")
    @Schema(description = "Document content snapshot")
    private String content;

    @NotBlank(message = "User ID is required")
    @Schema(description = "User ID who created this version")
    private String userId;

    @Schema(description = "Optional description", example = "Before major refactoring")
    private String description;
}
