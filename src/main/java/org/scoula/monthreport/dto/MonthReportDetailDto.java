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
    private String month;


    private BigDecimal totalExpense;

    private List<MonthExpenseDto> sixMonthChart;
    private List<CategoryRatioDto> categoryChart;
    private List<CategoryAmountDto> top3Spending;

    private org.scoula.monthreport.dto.AverageComparisonDto averageComparison;

    private org.scoula.monthreport.dto.PatternBannerDto patternBanner;

    private List<SpendingPatternDto> spendingPatterns; // 복수 패턴
    private String spendingPatternFeedback;

    private String nextGoal;
    private List<RecommendedChallengeDto> recommendedChallenges;
}
