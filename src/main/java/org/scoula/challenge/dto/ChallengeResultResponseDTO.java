package org.scoula.challenge.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChallengeResultResponseDTO {
    private String resultType; // SUCCESS_WIN, SUCCESS_EQUAL, FAIL
    private Integer actualRewardPoint;
    private Integer savedAmount; // 절약 금액 (goal_value - actual_value)
    private Object stockRecommendation; // 추천 주식 응답 (나중에 객체 교체 예정)
}
