package org.scoula.summary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySnapshotDto {
    private Long userId;
    private String month;               // 'YYYY-MM'
    private BigDecimal totalAsset;      // 월말 총자산
    private BigDecimal balance;         // 월 총수입
    private BigDecimal totalAmount;     // 월 총지출
    private LocalDateTime computedAt;   // 계산 시각
}
