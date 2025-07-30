package org.scoula.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChallengeCreateResponseDTO {
    private Long challengeId; // 생성된 챌린지 ID
    private String creatorNickname;
}
