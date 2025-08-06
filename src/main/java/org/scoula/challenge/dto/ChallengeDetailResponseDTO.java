package org.scoula.challenge.dto;

import lombok.Builder;
import lombok.Data;
import org.scoula.challenge.enums.ChallengeStatus;
import org.scoula.challenge.enums.ChallengeType;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ChallengeDetailResponseDTO {
    private Long id;
    private String title;
    private String description;
    private ChallengeType type;
    private ChallengeStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String goalType;
    private Integer goalValue;
    private Boolean isParticipating;
    private Boolean isMine;
    private Double myProgress;
    private Integer participantsCount;
    private Boolean isResultCheck;
    private String categoryName; // 추가

    // GROUP 전용
    private List<ChallengeMemberDTO> members;
}