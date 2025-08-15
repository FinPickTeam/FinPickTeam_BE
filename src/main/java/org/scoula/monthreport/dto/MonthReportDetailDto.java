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

    /** 이번 달 총 지출 */
    private BigDecimal totalExpense;

    /** 지난 달 총 지출(없으면 0으로 간주) */
    private BigDecimal lastMonthExpense;

    /**
     * 비교 금액 오버라이드가 필요할 때 사용하는 선택 필드.
     * 값이 있으면 getCompareExpense()가 이 값을 그대로 반환.
     */
    private BigDecimal compareExpenseOverride; // 새로 추가 사항

    private List<MonthExpenseDto> sixMonthChart;
    private List<CategoryRatioDto> categoryChart;
    private List<CategoryAmountDto> top3Spending;

    private org.scoula.monthreport.dto.AverageComparisonDto averageComparison;

    private List<SpendingPatternDto> spendingPatterns; // 복수 패턴
    private String spendingPatternFeedback;

    private String nextGoal;
    private List<RecommendedChallengeDto> recommendedChallenges;

    // 새로 추가 사항
    /**
     * 지난달 대비 증감 금액을 반환.
     * - compareExpenseOverride가 있으면 그 값을 그대로 씀
     * - 아니면 totalExpense - lastMonthExpense (널은 0으로 처리)
     */
    public BigDecimal getCompareExpense() {
        if (compareExpenseOverride != null) return compareExpenseOverride;
        BigDecimal thisExp = totalExpense != null ? totalExpense : BigDecimal.ZERO;
        BigDecimal lastExp = lastMonthExpense != null ? lastMonthExpense : BigDecimal.ZERO;
        return thisExp.subtract(lastExp);
    }
}
