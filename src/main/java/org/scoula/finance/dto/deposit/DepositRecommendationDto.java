package org.scoula.finance.dto.deposit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class DepositRecommendationDto {
    private String depositBankName; // 은행명
    private String depositProductName; // 상품명
    private String depositContractPeriod; // 계약기간
    private String depositSubscriptionAmount; // 가입금액
    private double depositBasicRate; // 기본 금리
    private double depositMaxRate; // 최고 금리
    private String depositPreferentialRate; //우대 이율
    private String depositProductFeatures; // 상품 특징
    private String depositSummary; // 상품 요약
    private String depositLink; // 상품 링크
}
