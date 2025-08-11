package org.scoula.challenge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.challenge.enums.ChallengeStatus;
import org.scoula.challenge.enums.ChallengeType;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeListResponseDTO {
    private Long id;
    private String title;

    private ChallengeType type;
    private ChallengeStatus status;   // 카드에서 상태 표시용
    private String categoryName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    // 목표/보상/인원
    private String goalType;          // 예: "소비"
    private Integer goalValue;
    private Integer maxParticipants;  // 예: GROUP=6, PERSONAL=1
    private Integer participantsCount;
    private Integer rewardPoint;

    // 참여 여부 & 진행률 & 결과확인 여부
    @JsonProperty("isParticipating")
    private Boolean participating;    // null-safe

    private Double myProgressRate;    // null 가능 (미참여/COMMON 등)

    @JsonProperty("isResultCheck")
    private Boolean resultChecked;    // null-safe

    // 내가 만든 챌린지인지
    @JsonProperty("isMine")
    private Boolean isMine;           // null-safe
}
