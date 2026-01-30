package com.collab.documentservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Share document request")
public class ShareDocumentRequest {

    @NotBlank(message = "User ID is required")
    @Schema(description = "User ID to share with")
    private String userId;

    @NotBlank(message = "Permission is required")
    @Pattern(regexp = "edit|view", message = "Permission must be 'edit' or 'view'")
    @Schema(description = "Permission level", example = "edit", allowableValues = {"edit", "view"})
    private String permission;
}
