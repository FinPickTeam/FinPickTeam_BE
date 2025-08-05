package org.scoula.monthreport.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LedgerTransactionDTO {
    private BigDecimal amount;
    private String categoryName;
}
