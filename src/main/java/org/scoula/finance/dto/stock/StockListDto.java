package org.scoula.finance.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class StockListDto {
    private String stockCode;
    private String stockName;
    private String stockReturnsData;
    private int stockPrice;
    private String stockMarketType;
    private String stockPredictedPrice;
    private String stockChangeRate;
    private String stockSummary;


}
