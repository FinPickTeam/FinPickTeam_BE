package org.scoula.monthreport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthReportDetailDto {

    private String month; // ex) "2025-07"

    private BigDecimal totalExpense;
    private BigDecimal compareExpense;

    private List<MonthExpenseDto> sixMonthChart;
    private List<CategoryRatioDto> categoryChart;
    private List<CategoryAmountDto> top3Spending;

    private String spendingPatternLabel;
    private String spendingPatternFeedback;

    private String nextGoal;

    private List<RecommendedChallengeDto> recommendedChallenges;
}

