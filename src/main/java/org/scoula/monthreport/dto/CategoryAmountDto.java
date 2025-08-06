package org.scoula.monthreport.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CategoryAmountDto {
    private String category;     // "식비"
    private BigDecimal amount;   // 금액
    private BigDecimal ratio;    // 퍼센트
}
