package org.scoula.coin.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoinStatusResponseDTO {
    private long amount;                 // 현재 포인트
    private long cumulativeAmount;       // 누적 포인트(챌린지+퀴즈)
    private long monthlyCumulativeAmount;// 이달 누적(챌린지)
    private String updatedAt;            // 갱신시각(선택)
}