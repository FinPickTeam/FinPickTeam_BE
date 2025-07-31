package org.scoula.monthreport.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonthReport {
    private Long id;
    private Long userId;
    private String month;
    private BigDecimal totalExpense;
    private BigDecimal totalSaving;
    private BigDecimal savingRate;
    private BigDecimal compareExpense;
    private BigDecimal compareSaving;
    private String categoryChart;
    private String sixMonthChart;
    private String feedback;
    private String nextGoal;
}