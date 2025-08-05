package org.scoula.monthreport.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MonthReportDTO {
    private String month;
    private BigDecimal totalExpense;
    private BigDecimal totalSaving;

    private BigDecimal compareExpense;
    private BigDecimal compareSaving;

    private String categoryChart;   // JSON string
    private String sixMonthChart;   // JSON string

    private String feedback;
    private String nextGoal;
}
