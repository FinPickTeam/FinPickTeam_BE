package org.scoula.monthreport.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonthReport {
    private Long userId;
    private String month; // YYYY-MM
    private BigDecimal totalExpense;
    private BigDecimal totalSaving;
    private BigDecimal savingRate;
    private BigDecimal compareExpense;
    private BigDecimal compareSaving;
    private String categoryChart; // JSON
    private String sixMonthChart; // JSON
    private String patternLabel;
    private String feedback;
    private String nextGoal;
}
