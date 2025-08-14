package org.scoula.monthreport.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.scoula.monthreport.domain.LedgerTransaction;
import org.scoula.monthreport.domain.MonthReport;
import org.scoula.monthreport.dto.AverageComparisonDto;
import org.scoula.monthreport.dto.RecommendationContext;
import org.scoula.monthreport.dto.RecommendedChallengeDto;
import org.scoula.monthreport.enums.SpendingPatternType;
import org.scoula.monthreport.ext.kosis.KosisFileBenchmarkProvider;
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

        List<LedgerTransaction> txList = monthReportMapper.findLedgerTransactions(userId, from, to);

        if (txList == null || txList.isEmpty()) {
            BigDecimal totalExpense = ZERO, totalSaving = ZERO, savingRate = ZERO;
            BigDecimal compareExpense = ZERO, compareSaving = ZERO;
            String categoryChart = "[]";
            String sixMonthChart = buildSixMonthChartJson(userId, month, totalExpense);
            String feedback = "소비 데이터가 부족합니다.";
            String nextGoalsJson = toJson(ChallengeRecommenderEngine.recommend());

            // 🔹 데이터 없으면 STABLE 저장
            String patternLabel = SpendingPatternType.STABLE.name();

            monthReportMapper.insertMonthReport(
                    userId, monthStr, totalExpense, totalSaving, savingRate,
                    compareExpense, compareSaving, categoryChart, sixMonthChart,
                    feedback, nextGoalsJson, patternLabel
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

        // 비중(%), uncategorized 제외
        Map<String, BigDecimal> ratioMap = categoryMap.entrySet().stream()
                .filter(e -> !isUncategorized(e.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> totalExpense.signum()==0 ? ZERO :
                                e.getValue().multiply(BigDecimal.valueOf(100))
                                        .divide(totalExpense, 1, RoundingMode.HALF_UP)
                ));

        String categoryChart = buildCategoryChartJson(categoryMap, totalExpense);

        // 4) 6개월 차트
        String sixMonthChart = buildSixMonthChartJson(userId, month, totalExpense);

        // 4-1) 거시라벨 + 서브패턴
        List<org.scoula.transactions.domain.Ledger> ledgersForEngine =
                monthReportMapper.findExpenseLedgersForReport(userId, from, to);

        BigDecimal totalSaving = new BigDecimal("300000"); // TODO: 동적 계산
        BigDecimal savingRate = calcSavingRate(totalExpense, totalSaving);

        // 전월/3개월 평균/변동성은 필요 시 Mapper로 보강 — 일단 0으로
        BigDecimal last3moAvgExpense = ZERO;
        BigDecimal volatility = ZERO;

        PatternClassification pc = new PatternClassifier()
                .classify(ledgersForEngine, savingRate, last3moAvgExpense, totalExpense, volatility);
        SpendingPatternType overall = pc.getOverall();

        // 5) 전월 비교치
        YearMonth prevMonth = month.minusMonths(1);
        MonthReport prev = monthReportMapper.findMonthReport(userId, prevMonth.toString());
        BigDecimal compareExpense = (prev != null) ? totalExpense.subtract(nvl(prev.getTotalExpense())) : ZERO;
        BigDecimal compareSaving = (prev != null) ? totalSaving.subtract(nvl(prev.getTotalSaving())) : ZERO;

        // 5-1) 벤치마크 비교(파일 기반, 39세이하가구 최신)
        AverageComparisonDto averageComparison = buildAverageComparisonFromKosis(categoryMap, totalExpense);

        // 6) 피드백 + 추천
        String baseFeedback = generateFeedback(categoryMap, totalExpense);

        RecommendationContext ctx = new RecommendationContext();
        ctx.overall = overall;
        ctx.patterns = pc.getPatterns();
        ctx.categoryRatios = ratioMap;
        ctx.averageDiffByCat = averageComparison.byCategory;

        List<RecommendedChallengeDto> rec = ChallengeRecommenderEngine.recommend(ctx);
        String nextGoalsJson = toJson(rec);

        // 7) 저장 (🔹 pattern_label=overall)
        monthReportMapper.insertMonthReport(
                userId, monthStr, totalExpense, totalSaving, savingRate,
                compareExpense, compareSaving, categoryChart, sixMonthChart,
                baseFeedback, nextGoalsJson, overall.name()
        );
    }

    private BigDecimal calcSavingRate(BigDecimal expense, BigDecimal saving) {
        BigDecimal denom = expense.add(saving);
        return denom.compareTo(ZERO)==0 ? ZERO :
                saving.divide(denom, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
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
        Map<String, BigDecimal> filtered = categoryMap.entrySet().stream()
                .filter(e -> !isUncategorized(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        BigDecimal filteredTotal = filtered.values().stream().reduce(ZERO, BigDecimal::add);

        List<Map<String, Object>> chart = filtered.entrySet().stream()
                .map(e -> {
                    Map<String, Object> obj = new LinkedHashMap<>();
                    obj.put("category", e.getKey());
                    obj.put("amount", e.getValue());
                    obj.put("ratio", filteredTotal.compareTo(ZERO) == 0 ? ZERO :
                            e.getValue().divide(filteredTotal, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100))
                                    .setScale(1, RoundingMode.HALF_UP));
                    return obj;
                })
                .sorted((a, b) -> ((BigDecimal) b.get("amount")).compareTo((BigDecimal) a.get("amount")))
                .collect(Collectors.toList());

        return toJson(chart);
    }

    private AverageComparisonDto buildAverageComparisonFromKosis(
            Map<String, BigDecimal> myCatTotals, BigDecimal myTotalExpense) {

        Map<String, BigDecimal> bm = KosisFileBenchmarkProvider
                .load("external/KOSISAverageMonthlyHousehold.json", "external/kosis_mapping.json");

        if (bm.isEmpty()) return new AverageComparisonDto(0, Map.of(), "비교 기준이 부족합니다.");

        BigDecimal avgTotal = bm.getOrDefault("total", ZERO);
        int totalDiff = avgTotal.signum()==0 ? 0 :
                myTotalExpense.subtract(avgTotal)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(avgTotal, 0, RoundingMode.HALF_UP).intValue();

        List<String> keys = List.of("food","cafe","shopping","mart","house","transport","subscription","etc");
        Map<String, Integer> byCat = new LinkedHashMap<>();
        for (String k : keys){
            BigDecimal mine = myCatTotals.getOrDefault(k, ZERO);
            BigDecimal avg  = bm.getOrDefault(k, ZERO);
            int diff = avg.signum()==0 ? 0 :
                    mine.subtract(avg).multiply(BigDecimal.valueOf(100))
                            .divide(avg, 0, RoundingMode.HALF_UP).intValue();
            byCat.put(k, diff);
        }

        String comment = (totalDiff>0 ? "동일 연령대 평균보다 " + totalDiff + "% 더 썼어요."
                : totalDiff<0 ? "동일 연령대 평균보다 " + Math.abs(totalDiff) + "% 덜 썼어요."
                : "또래와 유사한 지출이에요.");

        return new AverageComparisonDto(totalDiff, byCat, comment);
    }

    private String generateFeedback(Map<String, BigDecimal> categoryMap, BigDecimal totalExpense) {
        Map<String, BigDecimal> filtered = categoryMap.entrySet().stream()
                .filter(e -> !isUncategorized(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (filtered.isEmpty() || totalExpense.compareTo(ZERO) == 0)
            return "소비 데이터가 부족합니다.";

        Map.Entry<String, BigDecimal> max = filtered.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow();

        BigDecimal ratio = max.getValue().divide(
                filtered.values().stream().reduce(ZERO, BigDecimal::add),
                4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        if (ratio.compareTo(BigDecimal.valueOf(40)) > 0) {
            return max.getKey() + " 지출 비중이 높습니다. 한도를 정해보세요.";
        }
        return "이번 달 소비 패턴이 안정적입니다.";
    }

    private static boolean isUncategorized(String name) {
        if (name == null) return true;
        String n = name.trim();
        return n.isEmpty() || n.equalsIgnoreCase("uncategorized") || n.equals("카테고리 없음");
    }

    private String toJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); }
        catch (Exception e) { return "[]"; }
    }

    private static String safe(String s) { return s == null ? "" : s; }
    private static BigDecimal nvl(BigDecimal v) { return v == null ? ZERO : v; }
}
