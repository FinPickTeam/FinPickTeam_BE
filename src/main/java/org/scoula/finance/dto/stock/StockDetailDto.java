package org.scoula.finance.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class StockDetailDto {
    private int id;
    private String stockCode;
    private String stockName;
    private String stockPrice;
    private String stockPredictedPrice;
    private String stockChangeRate;
    private String stockChartData;
    private String stockYearHigh;
    private String stockYearLow;
    private String stockFaceValue;
    private String stockMarketCap;
    private String stockSalesAmount;
    private String stockPer;

}
