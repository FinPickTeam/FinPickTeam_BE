package org.scoula.monthreport.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.scoula.monthreport.domain.MonthReport;
import org.scoula.monthreport.dto.*;
import org.scoula.monthreport.enums.SpendingPatternType;
import org.scoula.monthreport.mapper.MonthReportMapper;
import org.scoula.monthreport.util.ChallengeRecommenderEngine;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MonthReportReadServiceImpl implements MonthReportReadService {

    private final MonthReportMapper monthReportMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // kosis 키 비중 맵 (transfer/없음/이체 제외)
    private Map<String, BigDecimal> toKosisRatioMap(List<CategoryRatioDto> chart){
        if (chart == null || chart.isEmpty()) return Map.of();
        Map<String, BigDecimal> sums = new LinkedHashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (var c : chart){
            String raw = c.getCategory()==null? "" : c.getCategory().trim();
            if ("이체".equals(raw)) continue; // ★ 한글 라벨 '이체' 제외
            if (org.scoula.monthreport.util.CategoryMapper.excludeOnAnalysis(raw)) continue;
            String key = org.scoula.monthreport.util.CategoryMapper.fromAny(raw);
            BigDecimal amt = nz(c.getAmount());
            sums.merge(key, amt, BigDecimal::add);
            total = total.add(amt);
        }
        if (total.compareTo(BigDecimal.ZERO)==0) return Map.of();
        Map<String, BigDecimal> out = new LinkedHashMap<>();
        for (var e : sums.entrySet()){
            BigDecimal r = e.getValue().multiply(BigDecimal.valueOf(100))
                    .divide(total, 1, RoundingMode.HALF_UP);
            out.put(e.getKey(), r);
        }
        return out;
    }

    @Override
    public MonthReportDetailDto getReport(Long userId, String month) {
        MonthReport report = monthReportMapper.findMonthReport(userId, month);
        if (report == null) throw new IllegalArgumentException("리포트가 존재하지 않습니다.");

        MonthReportDetailDto dto = new MonthReportDetailDto();
        dto.setMonth(month);
        dto.setTotalExpense(nz(report.getTotalExpense()));

        // ── 차트 파싱 ──
        List<CategoryRatioDto> categoryChart =
                parseJson(report.getCategoryChart(), new TypeReference<List<CategoryRatioDto>>() {});
        List<MonthExpenseDto> sixMonthChart =
                parseJson(report.getSixMonthChart(), new TypeReference<List<MonthExpenseDto>>() {});
        dto.setCategoryChart(categoryChart);
        dto.setSixMonthChart(sixMonthChart);

        // ── Top3(금액 기준, 재계산 금지) ──
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

        // ── 평균 비교(벤치마크): KOSIS 키로 정규화해서 비교 ──
        Map<String, BigDecimal> myCatTotalsKosis = normalizeToKosisKeysFromChart(categoryChart);
        dto.setAverageComparison(
                buildAverageComparisonFromKosis(myCatTotalsKosis, nz(report.getTotalExpense()))
        );

        // ── 패턴(읽기에서는 overall=DB, sub는 재계산) ──
        java.time.YearMonth ym = java.time.YearMonth.parse(month);
        var ledgers = monthReportMapper.findExpenseLedgersForReport(userId, ym.atDay(1), ym.atEndOfMonth());

        SpendingPatternType overallFromDb = safeOverall(report.getPatternLabel());

        var subOnly = new PatternClassifier().classify(
                ledgers, null, null, nz(report.getTotalExpense()), null
        );

        dto.setSpendingPatterns(
                List.of(new SpendingPatternDto(overallFromDb, subOnly.getPatterns()))
        );

        // ── 추천 ──
        var ratioMap = toKosisRatioMap(categoryChart);
        var ctx = new RecommendationContext();
        ctx.overall = overallFromDb;
        ctx.patterns = subOnly.getPatterns();
        ctx.categoryRatios = ratioMap;
        ctx.averageDiffByCat = dto.getAverageComparison() != null ? dto.getAverageComparison().byCategory : Map.of();
        dto.setRecommendedChallenges(ChallengeRecommenderEngine.recommend(ctx));

        // ── 나머지 그대로 ──
        dto.setNextGoal(report.getNextGoal());
        dto.setSpendingPatternFeedback(report.getFeedback());

        // 배너 조립
        dto.setPatternBanner(PatternBannerMapper.toBanner(
                new PatternClassification(overallFromDb, subOnly.getPatterns()),
                dto.getAverageComparison()
        ));

        return dto;
    }

    // ===== Helpers =====

    private SpendingPatternType safeOverall(String label) {
        try {
            return label == null ? SpendingPatternType.STABLE : SpendingPatternType.valueOf(label);
        } catch (Exception e) {
            return SpendingPatternType.STABLE;
        }
    }

    private <T> List<T> parseJson(String json, TypeReference<List<T>> typeRef) {
        try { return objectMapper.readValue(json, typeRef); }
        catch (Exception e) { return Collections.emptyList(); }
    }

    private BigDecimal nz(BigDecimal v){ return v==null? BigDecimal.ZERO : v; }

    // 한글 라벨 → KOSIS 키 합산(읽기 전용)
    private Map<String, BigDecimal> normalizeToKosisKeysFromChart(List<CategoryRatioDto> chart){
        Map<String, BigDecimal> out = new LinkedHashMap<>();
        if (chart == null) return out;
        for (var c : chart){
            String raw = c.getCategory()==null? "" : c.getCategory().trim();
            if ("이체".equals(raw)) continue; // ★ 한글 라벨 '이체' 제외
            if (org.scoula.monthreport.util.CategoryMapper.excludeOnAnalysis(raw)) continue;
            String key = org.scoula.monthreport.util.CategoryMapper.fromAny(raw);
            out.merge(key, nz(c.getAmount()), BigDecimal::add);
        }
        return out;
    }

    private AverageComparisonDto buildAverageComparisonFromKosis(
            Map<String, BigDecimal> myCatTotalsKosis, BigDecimal myTotalExpense){
        Map<String, BigDecimal> bm =
                org.scoula.monthreport.ext.kosis.KosisFileBenchmarkProvider
                        .load("external/KOSISAverageMonthlyHousehold.json", "external/kosis_mapping.json");
        if (bm.isEmpty()) return new AverageComparisonDto(0, Map.of(), "비교 기준이 부족합니다.");

        BigDecimal avgTotal = bm.getOrDefault("total", BigDecimal.ZERO);
        int totalDiff = avgTotal.signum()==0 ? 0 :
                myTotalExpense.subtract(avgTotal).multiply(BigDecimal.valueOf(100))
                        .divide(avgTotal, 0, RoundingMode.HALF_UP).intValue();

        List<String> keys = List.of("food","cafe","shopping","mart","house","transport","subscription","etc");
        Map<String, Integer> byCat = new LinkedHashMap<>();
        for (String k : keys){
            BigDecimal mine = myCatTotalsKosis.getOrDefault(k, BigDecimal.ZERO);
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
