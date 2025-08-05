package org.scoula.monthreport.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LedgerTransaction {
    private BigDecimal amount;
    private String categoryName;
}
