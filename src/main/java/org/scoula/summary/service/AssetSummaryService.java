
package org.scoula.summary.service;

import org.scoula.summary.dto.AssetSummaryCompareDto;

import java.math.BigDecimal;

public interface AssetSummaryService {
    BigDecimal getAccountTotal(Long userId);
    BigDecimal getCardTotal(Long userId);
    BigDecimal getTotalAsset(Long userId);
    AssetSummaryCompareDto getAssetSummaryCompare(Long userId);
}
