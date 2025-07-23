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
    private List<StockDailyPriceDto> stockChartData;
    private String stockPrice;
    private String stockPredictedPrice;
    private String stockChangeRate;
    private String stockSummary;


}
