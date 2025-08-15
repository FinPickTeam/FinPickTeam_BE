package org.scoula.summary.service;

import org.scoula.summary.dto.AssetSummaryCompareDto;

import java.math.BigDecimal;
import java.time.YearMonth;

public interface AssetSummaryService {
    BigDecimal getAccountTotal(Long userId);
    BigDecimal getCardTotal(Long userId); // 현재월 카드소비 (기존 호환)
    BigDecimal getTotalAsset(Long userId);
    BigDecimal getAssetChange(Long userId, YearMonth month);
    AssetSummaryCompareDto getAssetSummaryCompare(Long userId, YearMonth targetMonth);
}
