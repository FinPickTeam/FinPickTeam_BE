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
            String patternLabel = SpendingPatternType.STABLE.name();

            monthReportMapper.insertMonthReport(
                    userId, monthStr, totalExpense, totalSaving, savingRate,
                    compareExpense, compareSaving, categoryChart, sixMonthChart,
                    feedback, nextGoalsJson, patternLabel
            );
            return;
        }

        // 1) 합계/카테고리 집계(원본 라벨)
        BigDecimal totalExpense = txList.stream()
                .map(LedgerTransaction::getAmount)
                .reduce(ZERO, BigDecimal::add);

        Map<String, BigDecimal> rawCategoryMap = txList.stream()
                .collect(Collectors.groupingBy(
                        tx -> safe(tx.getCategoryName()),
                        Collectors.reducing(ZERO, LedgerTransaction::getAmount, BigDecimal::add)
                ));

        // DB/응답용: 한글 라벨 유지한 차트 (transfer/없음/이체 제외)
        String categoryChart = buildCategoryChartJson(rawCategoryMap, totalExpense);

        // 비교/분류용: KOSIS 키로 정규화 (transfer/없음/이체 제외)
        Map<String, BigDecimal> kosisCategoryMap = normalizeToKosisKeys(rawCategoryMap);

        // ★ 비중(%) — 분모는 "분석 포함 카테고리 합계"로 사용해야 왜곡 없음
        BigDecimal includedTotal = kosisCategoryMap.values().stream().reduce(ZERO, BigDecimal::add);
        Map<String, BigDecimal> ratioMap = kosisCategoryMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> includedTotal.signum() == 0 ? ZERO :
                                e.getValue().multiply(BigDecimal.valueOf(100))
                                        .divide(includedTotal, 1, RoundingMode.HALF_UP)
                ));

        // 2) 6개월 차트
        String sixMonthChart = buildSixMonthChartJson(userId, month, totalExpense);

        // 3) 분류 입력(원장 기반)
        List<org.scoula.transactions.domain.Ledger> ledgersForEngine =
                monthReportMapper.findExpenseLedgersForReport(userId, from, to);

        // TODO: 실제 적금/저축 합산으로 교체
        BigDecimal totalSaving = new BigDecimal("300000");
        BigDecimal savingRate = calcSavingRate(totalExpense, totalSaving);

        // 최근 3개월(현월 제외) 평균/표준편차
        Stat recent3 = recent3Stats(userId, month);
        BigDecimal last3moAvgExpense = recent3.avg;
        BigDecimal volatility = recent3.stddev;

        PatternClassification pc = new PatternClassifier()
                .classify(ledgersForEngine, savingRate, last3moAvgExpense, totalExpense, volatility);
        SpendingPatternType overall = pc.getOverall();

        // 증가율(%) 계산
        BigDecimal incPct = ZERO;
        if (last3moAvgExpense != null && last3moAvgExpense.signum() > 0) {
            incPct = totalExpense.subtract(last3moAvgExpense)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(last3moAvgExpense, 0, RoundingMode.HALF_UP);
        }

        // 4) 전월 비교치
        YearMonth prevMonth = month.minusMonths(1);
        MonthReport prev = monthReportMapper.findMonthReport(userId, prevMonth.toString());
        BigDecimal compareExpense = (prev != null) ? totalExpense.subtract(nvl(prev.getTotalExpense())) : ZERO;
        BigDecimal compareSaving = (prev != null) ? totalSaving.subtract(nvl(prev.getTotalSaving())) : ZERO;

        // 5) 평균 비교
        AverageComparisonDto averageComparison =
                buildAverageComparisonFromKosis(kosisCategoryMap, includedTotal); // ★ 포함합계를 분모로

        // 6) 피드백(한 줄)
        String baseFeedback = buildOneLineFeedback(overall, pc.getPatterns(), savingRate, incPct, averageComparison);

        // 7) 추천 컨텍스트
        RecommendationContext ctx = new RecommendationContext();
        ctx.overall = overall;
        ctx.patterns = pc.getPatterns();
        ctx.categoryRatios = ratioMap;
        ctx.averageDiffByCat = averageComparison.byCategory;
        List<RecommendedChallengeDto> rec = ChallengeRecommenderEngine.recommend(ctx);
        String nextGoalsJson = toJson(rec);

        // 8) 저장 (overall만 저장, 읽기에서 배너 조립)
        monthReportMapper.insertMonthReport(
                userId, monthStr, totalExpense, totalSaving, savingRate,
                compareExpense, compareSaving, categoryChart, sixMonthChart,
                baseFeedback, nextGoalsJson, overall.name()
        );
    }

    private BigDecimal calcSavingRate(BigDecimal expense, BigDecimal saving) {
        BigDecimal denom = expense.add(saving);
        return denom.compareTo(ZERO) == 0 ? ZERO
                : saving.divide(denom, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    private String buildOneLineFeedback(SpendingPatternType overall,
                                        Set<SpendingPatternType> sub,
                                        BigDecimal savingRate,
                                        BigDecimal incPct,
                                        AverageComparisonDto avg) {

        List<String> overLabels = new ArrayList<>();
        for (SpendingPatternType t : sub) if (t.name().endsWith("_OVER")) overLabels.add(t.getLabel());
        String overPart = overLabels.isEmpty() ? "" : " 특히 " + String.join(", ", overLabels) + " 비중이 높아요.";

        List<String> timeFlags = new ArrayList<>();
        if (sub.contains(SpendingPatternType.LATE_NIGHT)) timeFlags.add("심야 지출");
        if (sub.contains(SpendingPatternType.WEEKEND))    timeFlags.add("주말 지출");
        String timePart = timeFlags.isEmpty() ? "" : " 특히 " + String.join("·", timeFlags) + "이 높아요.";

        String avgPart = (avg != null && avg.getComment() != null && !avg.getComment().isBlank())
                ? " " + avg.getComment()
                : "";

        String msg;
        switch (overall) {
            case FRUGAL -> {
                String sr = savingRate == null ? "0" : savingRate.setScale(0, RoundingMode.HALF_UP).toPlainString();
                msg = "저축률 " + sr + "%로 절약형이에요." + avgPart;
            }
            case OVERSPENDER -> {
                String inc = incPct == null ? "0" : incPct.toPlainString();
                msg = "최근 3개월 평균 대비 +" + inc + "% 지출했어요." + overPart + avgPart;
            }
            case VOLATILE -> {
                msg = "이번 달 지출 변동성이 커요." + overPart + avgPart;
            }
            default -> {
                if (!overLabels.isEmpty()) {
                    msg = overPart.substring(1) + timePart; // “특히 …” 시작
                } else if (!timeFlags.isEmpty()) {
                    msg = timePart.substring(1);
                } else {
                    msg = "이번 달 소비 패턴이 안정적이에요." + avgPart;
                }
            }
        }
        return msg.trim();
    }

    /** 6개월 차트 JSON */
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

    /** 카테고리 차트(JSON, 한글 라벨 유지) */
    private String buildCategoryChartJson(Map<String, BigDecimal> categoryMap, BigDecimal total) {
        Map<String, BigDecimal> filtered = categoryMap.entrySet().stream()
                .filter(e -> {
                    String k = e.getKey();
                    // ★ '이체' 한글 라벨도 확실히 제외
                    if ("이체".equals(k)) return false;
                    return !org.scoula.monthreport.util.CategoryMapper.excludeOnCharts(k);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        BigDecimal filteredTotal = filtered.values().stream().reduce(ZERO, BigDecimal::add);

        List<Map<String, Object>> chart = filtered.entrySet().stream()
                .map(e -> {
                    Map<String, Object> obj = new LinkedHashMap<>();
                    obj.put("category", e.getKey());              // 한글 라벨 그대로 노출
                    obj.put("amount", e.getValue());
                    obj.put("ratio",
                            filteredTotal.compareTo(ZERO) == 0 ? ZERO :
                                    e.getValue().divide(filteredTotal, 4, RoundingMode.HALF_UP)
                                            .multiply(BigDecimal.valueOf(100))
                                            .setScale(1, RoundingMode.HALF_UP));
                    return obj;
                })
                .sorted((a, b) -> ((BigDecimal) b.get("amount")).compareTo((BigDecimal) a.get("amount")))
                .collect(Collectors.toList());

        return toJson(chart);
    }

    /** 평균 비교: KOSIS 벤치마크와 비교 (정규화 키 사용) */
    private AverageComparisonDto buildAverageComparisonFromKosis(
            Map<String, BigDecimal> myCatTotalsKosis, BigDecimal includedTotal) {

        Map<String, BigDecimal> bm = KosisFileBenchmarkProvider
                .load("external/KOSISAverageMonthlyHousehold.json", "external/kosis_mapping.json");

        if (bm.isEmpty()) return new AverageComparisonDto(0, Map.of(), "비교 기준이 부족합니다.");

        BigDecimal avgTotal = bm.getOrDefault("total", ZERO);
        int totalDiff = avgTotal.signum() == 0 ? 0 :
                includedTotal.subtract(avgTotal)  // ★ 동일 기준 비교
                        .multiply(BigDecimal.valueOf(100))
                        .divide(avgTotal, 0, RoundingMode.HALF_UP).intValue();

        List<String> keys = List.of("food","cafe","shopping","mart","house","transport","subscription","etc");
        Map<String, Integer> byCat = new LinkedHashMap<>();
        for (String k : keys){
            BigDecimal mine = myCatTotalsKosis.getOrDefault(k, ZERO);
            BigDecimal avg  = bm.getOrDefault(k, ZERO);
            int diff = avg.signum() == 0 ? 0 :
                    mine.subtract(avg).multiply(BigDecimal.valueOf(100))
                            .divide(avg, 0, RoundingMode.HALF_UP).intValue();
            byCat.put(k, diff);
        }

        String comment = (totalDiff > 0 ? "동일 연령대 평균보다 " + totalDiff + "% 더 썼어요."
                : totalDiff < 0 ? "동일 연령대 평균보다 " + Math.abs(totalDiff) + "% 덜 썼어요."
                : "또래와 유사한 지출이에요.");

        return new AverageComparisonDto(totalDiff, byCat, comment);
    }

    // ===== 유틸 =====

    private String toJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); }
        catch (Exception e) { return "[]"; }
    }

    private static String safe(String s) { return s == null ? "" : s; }
    private static BigDecimal nvl(BigDecimal v) { return v == null ? ZERO : v; }

    /** 한글 라벨 → KOSIS 키 정규화(생성용) */
    private Map<String, BigDecimal> normalizeToKosisKeys(Map<String, BigDecimal> raw) {
        Map<String, BigDecimal> out = new LinkedHashMap<>();
        raw.forEach((k, v) -> {
            // ★ 한글 '이체'도 제외
            if ("이체".equals(k)) return;
            if (org.scoula.monthreport.util.CategoryMapper.excludeOnAnalysis(k)) return;
            String key = org.scoula.monthreport.util.CategoryMapper.fromAny(k);
            out.merge(key, v, BigDecimal::add);
        });
        return out;
    }

    /** 최근 3개월(현월 제외) 평균/표준편차 */
    private Stat recent3Stats(Long userId, YearMonth currentMonth) {
        List<MonthReport> recentIncl = monthReportMapper.findRecentMonthReportsInclusive(
                userId, currentMonth.toString(), 4
        );
        if (recentIncl == null || recentIncl.isEmpty()) return new Stat(ZERO, ZERO);

        String cur = currentMonth.toString();
        List<BigDecimal> prev3 = recentIncl.stream()
                .filter(r -> !Objects.equals(r.getMonth(), cur))
                .sorted(Comparator.comparing(MonthReport::getMonth).reversed())
                .limit(3)
                .map(r -> nvl(r.getTotalExpense()))
                .collect(Collectors.toList());

        if (prev3.isEmpty()) return new Stat(ZERO, ZERO);

        BigDecimal sum = prev3.stream().reduce(ZERO, BigDecimal::add);
        BigDecimal avg = sum.divide(BigDecimal.valueOf(prev3.size()), 2, RoundingMode.HALF_UP);

        // 표준편차(샘플)
        BigDecimal variance = ZERO;
        for (BigDecimal x : prev3) {
            BigDecimal diff = x.subtract(avg);
            variance = variance.add(diff.multiply(diff));
        }
        variance = variance.divide(BigDecimal.valueOf(Math.max(prev3.size() - 1, 1)), 4, RoundingMode.HALF_UP);
        BigDecimal stddev = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()))
                .setScale(2, RoundingMode.HALF_UP);

        return new Stat(avg, stddev);
    }

    private record Stat(BigDecimal avg, BigDecimal stddev) {}
}
