package org.scoula.monthreport.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MonthExpenseDto {
    private String month;       // "2025-07"
    private BigDecimal amount;  // 지출 금액
}
