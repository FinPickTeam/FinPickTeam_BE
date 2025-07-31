package org.scoula.monthreport.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonthReportDTO {
    private String month;
    private BigDecimal totalExpense;
    private BigDecimal totalSaving;
}