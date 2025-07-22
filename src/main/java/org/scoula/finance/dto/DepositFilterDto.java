package org.scoula.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class DepositFilterDto {
    private String bankName; // 은행명
    private Integer contractPeriodMonth; // 계약 기간 (개월 수)
    private Integer minSubscriptionAmount; // 최소 가입 금액 (단위: 원)
    private String rateOrder; // 금리 정렬 순서: "asc" 또는 "desc"
}
