package org.scoula.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.challenge.enums.ChallengeType;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeHistoryItemDTO {
    private Long challengeId;
    private String title;
    private String categoryName;

    private ChallengeType type;
    private LocalDate startDate;
    private LocalDate endDate;

    private Boolean isSuccess;           // user_challenge.is_success (NULL 가능)
    private Boolean isCompleted;         // 항상 true (is_completed=1만 조회)
    private Integer actualValue;         // 내 실제 소비값
    private Integer goalValue;           // 목표값 (challenge.goal_value)
    private Integer actualRewardPoint;   // 지급 포인트
    private Boolean resultChecked;       // 결과 확인 여부
    private LocalDate completedAt;   // user_challenge.updated_at
}
