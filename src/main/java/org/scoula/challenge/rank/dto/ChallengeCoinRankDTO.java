package org.scoula.challenge.rank.dto;

import lombok.Data;

@Data
public class ChallengeCoinRankDTO {
    private Long userId;
    private String nickname;
    private String month;
    private int totalCoin;
    private Integer totalChallenges;
    private Integer successChallenges;
    private Double successRate; // 성공률(%)
}
