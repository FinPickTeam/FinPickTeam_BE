package org.scoula.summary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class AssetSummaryCompareDto {
    private BigDecimal currentAssetTotal; // 이번달 자산 (계좌+카드)
    private BigDecimal prevAssetTotal; // 지난달 자산 (계좌+카드)
    private BigDecimal assetDiff; // 자산 변화
    private BigDecimal currentSpending; // 이번달 카드소비
    private BigDecimal prevSpending; // 지난달 카드소비
    private BigDecimal spendingDiff; // 소비 변화
}
