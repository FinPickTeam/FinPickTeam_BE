package org.scoula.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder                // ★ 빌더 추가
@NoArgsConstructor      // ★ 직렬화/역직렬화 대비
@AllArgsConstructor
public class StockRecommendationDTO {
    private String stockCode;
    private String stockName;
    private String stockReturnsData;
    private int    stockPrice;
    private String stockMarketType;
    private String stockPredictedPrice;
    private String stockChangeRate;
    private String stockSummary;
}