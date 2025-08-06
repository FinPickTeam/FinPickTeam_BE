package org.scoula.challenge.rank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ChallengeCoinRankResponseDTO {
    private Long userId;
    private String nickname;
    private int rank;
    private long cumulativePoint;
    private int challengeCount;
}
