package com.collab.versionservice.dto;

import com.collab.versionservice.model.Version;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Version data transfer object")
public class VersionDTO {

    @Schema(description = "Version ID")
    private String id;

    @Schema(description = "Document ID")
    private String documentId;

    @Schema(description = "Content snapshot")
    private String content;

    @Schema(description = "User ID who created this version")
    private String userId;

    @Schema(description = "Creation timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "Version description")
    private String description;

    @Schema(description = "Change statistics")
    private ChangeStatsDTO changeStats;

    public static VersionDTO fromVersion(Version version) {
        ChangeStatsDTO statsDTO = new ChangeStatsDTO(
            version.getChangeStats().getCharsAdded(),
            version.getChangeStats().getCharsDeleted(),
            version.getChangeStats().getTotalChanges()
        );

        return new VersionDTO(
            version.getId().toString(),
            version.getDocumentId().toString(),
            version.getContent(),
            version.getUserId().toString(),
            version.getTimestamp(),
            version.getDescription(),
            statsDTO
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeStatsDTO {
        private int charsAdded;
        private int charsDeleted;
        private int totalChanges;
    }
}
