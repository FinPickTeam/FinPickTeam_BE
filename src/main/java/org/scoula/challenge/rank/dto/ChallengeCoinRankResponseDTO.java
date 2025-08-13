package org.scoula.challenge.rank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 현재 달 코인 랭킹 응답 DTO (Top5 + 내 정보 포함) */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeCoinRankResponseDTO {
    private Long userId;
    private String nickname;
    private int rank;
    private long cumulativePoint;   // 이번 달 누적 포인트 (coin.monthly_cumulative_amount 기준 집계/랭킹 반영 결과)
    private int challengeCount;     // 지금까지 참여한 챌린지 개수 (user_challenge_summary.total_challenges)

    // (C) 성공률 관련 필드 추가
    private int successCount;       // user_challenge_summary.success_count
    private int totalChallenges;    // user_challenge_summary.total_challenges
    private int successRate;        // (success_count / total_challenges * 100) 반올림
}
