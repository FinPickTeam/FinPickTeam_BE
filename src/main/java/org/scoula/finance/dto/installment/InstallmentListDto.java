package org.scoula.finance.dto.installment;

public class InstallmentListDto {
    private String installmentBankName; // 은행명
    private String installmentProductName; // 상품명
    private String installmentContractPeriod; // 계약기간
    private String installmentSubscriptionAmount; // 가입금액
    private double installmentBasicRate; // 기본 금리
    private double installmentMaxRate; // 최고 금리
    private String installmentSummary; // 상품 요약
}
