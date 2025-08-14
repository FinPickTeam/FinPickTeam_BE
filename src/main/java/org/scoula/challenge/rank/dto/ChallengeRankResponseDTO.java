package org.scoula.challenge.rank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChallengeRankResponseDTO {
    private Long userId;
    private String nickname;
    private int rank;
    private int actualValue;
}
