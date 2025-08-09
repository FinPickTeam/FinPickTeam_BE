package org.scoula.coin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder
@AllArgsConstructor @NoArgsConstructor
public class CoinMonthlyResponseDTO {
    private String month;     // 예: "2025-08"
    private long amount;      // 이달 누적 포인트
    private String updatedAt; // 최근 갱신 시각 (선택)
}
