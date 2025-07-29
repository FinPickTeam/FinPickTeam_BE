package org.scoula.finance.dto.installment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class InstallmentDetailDto {
    private String installmentBankName; // 은행명
    private String installmentProductName; // 상품명
    private String installmentContractPeriod; // 계약기간
    private String installmentType; // 적립 방식
    private String installmentSubscriptionAmount; // 가입금액
    private double installmentBasicRate; // 기본 금리
    private double installmentMaxRate; // 최고 금리
    private String installmentPreferentialRate; //우대 이율
    private String installmentProductFeatures; // 상품 특징
    private String installmentSummary; // 상품 요약
    private String installmentLink; // 상품 링크
}
