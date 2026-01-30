package com.collab.versionservice.dto;

import com.collab.versionservice.model.Contribution;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User contribution statistics")
public class ContributionDTO {

    @Schema(description = "User ID")
    private String userId;

    @Schema(description = "Username")
    private String username;

    @Schema(description = "Contribution statistics")
    private ContributionStatsDTO stats;

    public static ContributionDTO fromContribution(Contribution contribution) {
        ContributionStatsDTO statsDTO = new ContributionStatsDTO(
            contribution.getStats().getEditsCount(),
            contribution.getStats().getCharsAdded(),
            contribution.getStats().getCharsDeleted(),
            contribution.getStats().getVersionsCreated()
        );

        return new ContributionDTO(
            contribution.getUserId().toString(),
            contribution.getUsername(),
            statsDTO
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContributionStatsDTO {
        private int editsCount;
        private int charsAdded;
        private int charsDeleted;
        private int versionsCreated;
    }
}
