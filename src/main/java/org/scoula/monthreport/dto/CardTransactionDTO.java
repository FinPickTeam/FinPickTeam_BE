package org.scoula.monthreport.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CardTransactionDTO {
    private BigDecimal amount;
    private String merchantIndustry;
    private String merchantIndustryCode;
}
