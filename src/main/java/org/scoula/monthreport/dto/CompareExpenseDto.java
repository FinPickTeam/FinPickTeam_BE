package org.scoula.monthreport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompareExpenseDto {
    private BigDecimal amount;
    private String type; // "INCREASE" or "DECREASE"
    private String text; // 예: "지난달보다 10만원 덜 썼어요!"
    private boolean highlight;
}

