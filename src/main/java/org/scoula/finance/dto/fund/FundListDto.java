package org.scoula.finance.dto.fund;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class FundListDto {
    private String fundManager; // 펀드 운용사
    private String fundProductName; // 펀드명
    private String fundRiskLevel; // 위험등급
    private String fundType; // 종류
    private String fund3MonthReturn; // 3개월 수익률
    private String fundReturnsData; // 수익률 데이터
    private String fundStartDate; // 설정일
    private String fundNetAssetValue; // 총자산
    private String fundTotalExpenseRatio; // 총보수
    private String fundProductFeatures; // 상품 특징
    private String fundLink; // 상품 링크
}
