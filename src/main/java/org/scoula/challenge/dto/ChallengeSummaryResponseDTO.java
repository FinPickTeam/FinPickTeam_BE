package org.scoula.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeSummaryResponseDTO {
    private int totalChallenges;
    private int successCount;
    private double achievementRate;
}
