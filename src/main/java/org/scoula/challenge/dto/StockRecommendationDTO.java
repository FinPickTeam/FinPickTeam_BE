package org.scoula.challenge.dto;

import lombok.Data;

@Data
public class StockRecommendationDTO {
    private String stockCode;
    private String stockName;
    private String stockReturnsData;
    private int stockPrice;
    private String stockMarketType;
    private String stockPredictedPrice;
    private String stockChangeRate;
    private String stockSummary;
}
