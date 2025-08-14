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
        if (report == null) {
            throw new IllegalArgumentException("리포트가 존재하지 않습니다.");
        }

        MonthReportDetailDto dto = new MonthReportDetailDto();
        dto.setMonth(month);
        dto.setTotalExpense(report.getTotalExpense());

        dto.setCategoryChart(parseJson(report.getCategoryChart(), new TypeReference<List<CategoryRatioDto>>() {}));
        dto.setSixMonthChart(parseJson(report.getSixMonthChart(), new TypeReference<List<MonthExpenseDto>>() {}));

        // top 3 카테고리는 categoryChart에서 상위 3개 추출
        List<CategoryRatioDto> categoryChart = dto.getCategoryChart();
        List<CategoryAmountDto> top3 = categoryChart.stream()
                .sorted((a, b) -> b.getRatio().compareTo(a.getRatio()))
                .limit(3)
                .map(c -> {
                    CategoryAmountDto a = new CategoryAmountDto();
                    a.setCategory(c.getCategory());
                    a.setAmount(calculateAmountFromRatio(report.getTotalExpense(), c.getRatio()));
                    a.setRatio(c.getRatio());
                    return a;
                }).toList();
        dto.setTop3Spending(top3);

        // 평균 비교(벤치마크)
        Map<String, BigDecimal> myCatTotals = toCategoryAmountMap(dto.getCategoryChart());
        dto.setAverageComparison(
                buildAverageComparisonFromKosis(myCatTotals, report.getTotalExpense())
        );

        // 🔄 월 거래를 엔진 도메인으로 바로 조회(어댑터 제거)
        java.time.YearMonth ym = java.time.YearMonth.parse(month);
        var ledgers = monthReportMapper.findExpenseLedgersForReport(userId, ym.atDay(1), ym.atEndOfMonth());

        var pc = new PatternClassifier().classify(
                ledgers, report.getSavingRate(), BigDecimal.ZERO, report.getTotalExpense(), BigDecimal.ZERO);

        dto.setSpendingPatterns(List.of(
                new org.scoula.monthreport.dto.SpendingPatternDto(pc.getOverall(), pc.getPatterns())
        ));

        // 추천 컨텍스트 구성
        var ratioMap = toCategoryRatioMap(dto.getCategoryChart()); // categoryChart의 ratio 사용
        var ctx = new org.scoula.monthreport.dto.RecommendationContext();
        ctx.overall = pc.getOverall();
        ctx.patterns = pc.getPatterns();
        ctx.categoryRatios = ratioMap;
        ctx.averageDiffByCat = dto.getAverageComparison() != null ? dto.getAverageComparison().byCategory : Map.of();

        dto.setRecommendedChallenges(org.scoula.monthreport.util.ChallengeRecommenderEngine.recommend(ctx));
        dto.setNextGoal(report.getNextGoal());

        // feedback은 DB 저장값 그대로 사용
        dto.setSpendingPatternFeedback(report.getFeedback());
        return dto;
    }

    private <T> List<T> parseJson(String json, TypeReference<List<T>> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private BigDecimal calculateAmountFromRatio(BigDecimal total, BigDecimal ratio) {
        // 레거시 상수 대신 RoundingMode 사용
        return total.multiply(ratio).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
    }

    private String generateSpendingAdvice(String feedback) {
        if (feedback.contains("식비") || feedback.contains("카페")) {
            return "다음 달 식비와 카페 지출을 약 15% 줄여보는 걸 추천드려요.";
        }
        return "안정적인 소비를 유지해보세요.";
    }

    private List<RecommendedChallengeDto> buildRecommendedChallenges(String feedback, BigDecimal totalExpense) {
        return List.of(
                new RecommendedChallengeDto("저축률 회복하기", "최소 450,000원 저축해보아요."),
                new RecommendedChallengeDto("식비 + 카페 지출 줄이기", "총합 350,000원 이하로 유지해보세요."),
                new RecommendedChallengeDto("무지출 데이 도전!", "‘무지출 데이’를 2회 이상 가져보세요.")
        );
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
