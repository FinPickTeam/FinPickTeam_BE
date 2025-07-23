package org.scoula.finance.dto.deposit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class DepositListDto {
    private String depositBankName; // 은행명
    private String depositProductName; // 상품명
    private String depositContractPeriod; // 계약기간
    private String depositSubscriptionAmount; // 가입금액
    private double depositBasicRate; // 기본 금리
    private double depositMaxRate; // 최고 금리
    private String depositSummary; // 상품 요약
}
