package org.scoula.challenge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import org.scoula.challenge.enums.ChallengeType;

import java.time.LocalDate;

@Data
@Builder
public class ChallengeListResponseDTO {
    private Long id;
    private String title;
    private ChallengeType type;
    private String categoryName; // 추가

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private boolean isParticipating; // 나의 참여 여부
    private Double myProgressRate;   // 개인/소그룹 챌린지일 경우
    private int participantsCount;   // 모집중인 경우

    private Boolean isResultCheck;
}
