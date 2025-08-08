package org.scoula.finance.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class StockCardListDto {
    private String stockCode;
    private String stockName;
    private String currentPrice;
    private String stockPredictedPrice;
    private String stockChangeRate;
    private String stockSummary;
}
