package org.scoula.monthreport.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.scoula.monthreport.domain.LedgerTransaction;
import org.scoula.monthreport.domain.MonthReport;
import org.scoula.monthreport.dto.RecommendedChallengeDto;
import org.scoula.monthreport.mapper.MonthReportMapper;
import org.scoula.monthreport.util.ChallengeRecommenderEngine;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonthReportGeneratorImpl implements MonthReportGenerator {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int SIX_MONTH_WINDOW = 6;

    private final MonthReportMapper monthReportMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void generate(Long userId, String monthStr) {
        YearMonth month = YearMonth.parse(monthStr);
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        // 1) 활성 계좌/카드만 포함된 원천 데이터 (SQL에서 필터 완료)
        List<LedgerTransaction> txList = monthReportMapper.findLedgerTransactions(userId, from, to);

        // 2) 거래 0건이어도 0값 리포트 생성 + 현월 포함 6개월 차트
        if (txList == null || txList.isEmpty()) {
            BigDecimal totalExpense = ZERO;
            BigDecimal totalSaving  = ZERO;
            BigDecimal savingRate   = ZERO;
            BigDecimal compareExpense = ZERO;
            BigDecimal compareSaving  = ZERO;
            String categoryChart = "[]";
            String sixMonthChart = buildSixMonthChartJson(userId, month, totalExpense);
            String feedback = "소비 데이터가 부족합니다.";

            String nextGoalsJson = toJson(ChallengeRecommenderEngine.recommend()); // 챌린지 2개

            monthReportMapper.insertMonthReport(
                    userId, monthStr, totalExpense, totalSaving, savingRate,
                    compareExpense, compareSaving, categoryChart, sixMonthChart,
                    feedback, nextGoalsJson
            );
            return;
        }

        // 3) 합계/카테고리 집계
        BigDecimal totalExpense = txList.stream()
                .map(LedgerTransaction::getAmount)
                .reduce(ZERO, BigDecimal::add);

        Map<String, BigDecimal> categoryMap = txList.stream()
                .collect(Collectors.groupingBy(
                        tx -> safe(tx.getCategoryName()),
                        Collectors.reducing(ZERO, LedgerTransaction::getAmount, BigDecimal::add)
                ));

        String categoryChart = buildCategoryChartJson(categoryMap, totalExpense);

        // 4) 6개월 차트: 현월 강제 포함
        String sixMonthChart = buildSixMonthChartJson(userId, month, totalExpense);

        // 5) 전월 비교치
        YearMonth prevMonth = month.minusMonths(1);
        MonthReport prev = monthReportMapper.findMonthReport(userId, prevMonth.toString());

        BigDecimal compareExpense = (prev != null) ? totalExpense.subtract(nvl(prev.getTotalExpense())) : ZERO;
        BigDecimal totalSaving = new BigDecimal("300000"); // TODO: 동적 계산/설정
        BigDecimal compareSaving = (prev != null) ? totalSaving.subtract(nvl(prev.getTotalSaving())) : ZERO;

        BigDecimal denominator = totalExpense.add(totalSaving);
        BigDecimal savingRate = (denominator.compareTo(ZERO) == 0)
                ? ZERO
                : totalSaving.divide(denominator, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        // 6) 피드백 + 챌린지 2개 (간단 버전)
        String feedback = generateFeedback(categoryMap, totalExpense);
        List<RecommendedChallengeDto> rec = ChallengeRecommenderEngine.recommend();
        String nextGoalsJson = toJson(rec);

        // 7) 저장
        monthReportMapper.insertMonthReport(
                userId, monthStr, totalExpense, totalSaving, savingRate,
                compareExpense, compareSaving, categoryChart, sixMonthChart,
                feedback, nextGoalsJson
        );
    }

    /** 6개월 차트: currentMonth 기준 과거 5개월 + 현월(합계가 0이어도 포함) */
    private String buildSixMonthChartJson(Long userId, YearMonth currentMonth, BigDecimal currentTotalExpense) {
        List<MonthReport> recent = monthReportMapper.findRecentMonthReportsInclusive(
                userId, currentMonth.toString(), SIX_MONTH_WINDOW
        );

        Map<String, BigDecimal> byMonth = (recent == null ? List.<MonthReport>of() : recent).stream()
                .collect(Collectors.toMap(
                        MonthReport::getMonth,
                        r -> nvl(r.getTotalExpense()),
                        (a, b) -> a
                ));

        List<Map<String, Object>> chart = new ArrayList<>();
        for (int i = SIX_MONTH_WINDOW - 1; i >= 0; i--) {
            YearMonth ym = currentMonth.minusMonths(i);
            String key = ym.toString();
            BigDecimal amount = byMonth.getOrDefault(
                    key, ym.equals(currentMonth) ? nvl(currentTotalExpense) : ZERO
            );
            Map<String, Object> obj = new LinkedHashMap<>();
            obj.put("month", key);
            obj.put("amount", amount);
            chart.add(obj);
        }
        return toJson(chart);
    }

    private String buildCategoryChartJson(Map<String, BigDecimal> categoryMap, BigDecimal total) {
        List<Map<String, Object>> chart = categoryMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> obj = new LinkedHashMap<>();
                    obj.put("category", e.getKey());
                    obj.put("amount", e.getValue());
                    obj.put("ratio", total.compareTo(ZERO) == 0 ? ZERO :
                            e.getValue().divide(total, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100))
                                    .setScale(1, RoundingMode.HALF_UP));
                    return obj;
                })
                .sorted((a, b) -> ((BigDecimal) b.get("amount")).compareTo((BigDecimal) a.get("amount")))
                .collect(Collectors.toList());

        return toJson(chart);
    }

    private String generateFeedback(Map<String, BigDecimal> categoryMap, BigDecimal totalExpense) {
        if (categoryMap.isEmpty() || totalExpense.compareTo(ZERO) == 0)
            return "소비 데이터가 부족합니다.";

        Map.Entry<String, BigDecimal> max = categoryMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow();

        BigDecimal ratio = max.getValue().divide(totalExpense, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        if (ratio.compareTo(BigDecimal.valueOf(40)) > 0) {
            return max.getKey() + " 지출 비중이 높습니다. 한도를 정해보세요.";
        }
        return "이번 달 소비 패턴이 안정적입니다.";
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }
    private static BigDecimal nvl(BigDecimal v) { return v == null ? ZERO : v; }
}
