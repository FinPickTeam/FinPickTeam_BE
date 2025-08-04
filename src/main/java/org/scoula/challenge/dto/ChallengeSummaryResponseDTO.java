package org.scoula.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ChallengeSummaryResponseDTO {
    private int totalChallenges;
    private int successCount;
    private double achievementRate;
}
