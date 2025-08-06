package org.scoula.challenge.rank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChallengeRankSnapshotResponseDTO {
    private String nickname;
    private int rank;
    private int actualValue;
    private String month;
    private boolean isChecked;
}
