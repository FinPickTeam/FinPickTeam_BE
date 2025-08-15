package org.scoula.monthreport.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.scoula.monthreport.domain.MonthReport;
import org.scoula.monthreport.dto.*;
import org.scoula.monthreport.mapper.MonthReportMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MonthReportReadServiceImpl implements MonthReportReadService {

    private final MonthReportMapper monthReportMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public MonthReportDetailDto getReport(Long userId, String month) {
        MonthReport report = monthReportMapper.findMonthReport(userId, month);
        if (report == null) throw new IllegalArgumentException("리포트가 존재하지 않습니다.");

        MonthReportDetailDto dto = new MonthReportDetailDto();
        dto.setMonth(month);
        dto.setTotalExpense(nz(report.getTotalExpense()));

        // 차트 파싱
        List<CategoryRatioDto> categoryChart =
                parseJson(report.getCategoryChart(), new TypeReference<List<CategoryRatioDto>>() {});
        List<MonthExpenseDto> sixMonthChart =
                parseJson(report.getSixMonthChart(), new TypeReference<List<MonthExpenseDto>>() {});
        dto.setCategoryChart(categoryChart);
        dto.setSixMonthChart(sixMonthChart);


        // ✅ top3: amount 기준, 재계산 금지
        List<CategoryAmountDto> top3 = categoryChart.stream()
                .sorted(Comparator.comparing(CategoryRatioDto::getAmount).reversed())
                .limit(3)
                .map(c -> {
                    CategoryAmountDto a = new CategoryAmountDto();
                    a.setCategory(c.getCategory());
                    a.setAmount(nz(c.getAmount()));
                    a.setRatio(nz(c.getRatio()));
                    return a;
                })
                .toList();
        dto.setTop3Spending(top3);

        // 평균 비교(벤치마크) — 기존 구현 그대로
        Map<String, BigDecimal> myCatTotals = toCategoryAmountMap(categoryChart);
        dto.setAverageComparison(
                buildAverageComparisonFromKosis(myCatTotals, nz(report.getTotalExpense()))
        );

        // 패턴/추천 기존 로직 유지
        java.time.YearMonth ym = java.time.YearMonth.parse(month);
        var ledgers = monthReportMapper.findExpenseLedgersForReport(userId, ym.atDay(1), ym.atEndOfMonth());
        var pc = new PatternClassifier().classify(
                ledgers, nz(report.getSavingRate()), BigDecimal.ZERO, nz(report.getTotalExpense()), BigDecimal.ZERO);

        dto.setSpendingPatterns(List.of(new SpendingPatternDto(pc.getOverall(), pc.getPatterns())));

        var ratioMap = toCategoryRatioMap(categoryChart);
        var ctx = new RecommendationContext();
        ctx.overall = pc.getOverall();
        ctx.patterns = pc.getPatterns();
        ctx.categoryRatios = ratioMap;
        ctx.averageDiffByCat = dto.getAverageComparison() != null ? dto.getAverageComparison().byCategory : Map.of();

        dto.setRecommendedChallenges(org.scoula.monthreport.util.ChallengeRecommenderEngine.recommend(ctx));
        dto.setNextGoal(report.getNextGoal());
        dto.setSpendingPatternFeedback(report.getFeedback());
        return dto;
    }

    private <T> List<T> parseJson(String json, TypeReference<List<T>> typeRef) {
        try { return objectMapper.readValue(json, typeRef); }
        catch (Exception e) { return Collections.emptyList(); }
    }

    private BigDecimal nz(BigDecimal v){ return v==null? BigDecimal.ZERO : v; }

    private String deriveMainCategory(List<CategoryRatioDto> chart){
        if (chart == null || chart.isEmpty()) return "";
        return chart.stream()
                .filter(c -> c.getCategory()!=null && !c.getCategory().isBlank())
                .max(Comparator.comparing(CategoryRatioDto::getAmount))
                .map(CategoryRatioDto::getCategory)
                .orElse("");
    }

    private Map<String, BigDecimal> toCategoryAmountMap(List<CategoryRatioDto> chart){
        if (chart == null) return Map.of();
        Map<String, BigDecimal> out = new LinkedHashMap<>();
        for (var c : chart) out.put(c.getCategory(), c.getAmount());
        return out;
    }

    private Map<String, BigDecimal> toCategoryRatioMap(List<CategoryRatioDto> chart){
        if (chart == null) return Map.of();
        Map<String, BigDecimal> out = new LinkedHashMap<>();
        for (var c : chart) out.put(c.getCategory(), c.getRatio());
        return out;
    }

    private AverageComparisonDto buildAverageComparisonFromKosis(
            Map<String, BigDecimal> myCatTotals, BigDecimal myTotalExpense){
        java.util.Map<String, java.math.BigDecimal> bm =
                org.scoula.monthreport.ext.kosis.KosisFileBenchmarkProvider
                        .load("external/KOSISAverageMonthlyHousehold.json", "external/kosis_mapping.json");
        if (bm.isEmpty()) return new AverageComparisonDto(0, Map.of(), "비교 기준이 부족합니다.");

        java.math.BigDecimal avgTotal = bm.getOrDefault("total", BigDecimal.ZERO);
        int totalDiff = avgTotal.signum()==0 ? 0 :
                myTotalExpense.subtract(avgTotal).multiply(BigDecimal.valueOf(100))
                        .divide(avgTotal, 0, RoundingMode.HALF_UP).intValue();

        List<String> keys = List.of("food","cafe","shopping","mart","house","transport","subscription","etc");
        Map<String, Integer> byCat = new LinkedHashMap<>();
        for (String k : keys){
            BigDecimal mine = myCatTotals.getOrDefault(k, BigDecimal.ZERO);
            BigDecimal avg  = bm.getOrDefault(k, BigDecimal.ZERO);
            int diff = avg.signum()==0 ? 0 :
                    mine.subtract(avg).multiply(BigDecimal.valueOf(100))
                            .divide(avg, 0, RoundingMode.HALF_UP).intValue();
            byCat.put(k, diff);
        }
        String comment = (totalDiff>0 ? "동일 연령대 평균보다 "+totalDiff+"% 더 썼어요."
                : totalDiff<0 ? "동일 연령대 평균보다 "+Math.abs(totalDiff)+"% 덜 썼어요."
                : "또래와 유사한 지출이에요.");
        return new AverageComparisonDto(totalDiff, byCat, comment);
    }
}
