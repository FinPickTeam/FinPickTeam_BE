package org.scoula.finance.dto.installment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class InstallmentFilterDto {
    private String installmentBankName; // 해당 은행만 조회
    private String installmentType; // 상품 타입 (자유적립식, 정액적립식)
    private String sortByInstallmentProductName; //상품 이름 순 asc or desc or null
    private String sortByInstallmentBasicRate; // 기본금리 순 asc or desc or null
}
