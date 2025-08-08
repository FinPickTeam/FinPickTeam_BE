package org.scoula.finance.dto.installment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class InstallmentListDto {
    private Long id;
    private String installmentBankName; // 은행명
    private String installmentProductName; // 상품명
    private String installmentType; // 적립 방식
    private double installmentBasicRate; // 기본 금리
    private double installmentMaxRate; // 최고 금리
    private String installmentSummary; // 상품 요약
}
