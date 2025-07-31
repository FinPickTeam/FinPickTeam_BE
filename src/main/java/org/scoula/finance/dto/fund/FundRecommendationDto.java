package org.scoula.finance.dto.fund;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class FundRecommendationDto {
    private String fundManager; // 펀드 운용사
    private String fundProductName; // 펀드명
    private String fundRiskLevel; // 위험등급
    private String fundType; // 종류
    private String fundReturnsData; // 수익률 데이터
    private String fundProductFeatures; // 상품 특징
}
