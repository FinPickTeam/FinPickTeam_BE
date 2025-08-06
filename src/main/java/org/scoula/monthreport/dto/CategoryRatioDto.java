package org.scoula.monthreport.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CategoryRatioDto {
    private String category;
    private BigDecimal amount;
    private BigDecimal ratio;
}
