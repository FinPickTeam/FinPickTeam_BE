package org.scoula.monthreport.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.scoula.monthreport.dto.*;
import org.scoula.monthreport.mapper.MonthReportMapper;
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

    private final MonthReportMapper monthReportMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void generate(Long userId, String monthStr) {
        YearMonth month = YearMonth.parse(monthStr);
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        List<LedgerTransactionDTO> txList = monthReportMapper.findLedgerTransactions(userId, from, to);
        if (txList.isEmpty()) return;

        // 총 지출 계산
        BigDecimal totalExpense = txList.stream()
                .map(LedgerTransactionDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 카테고리별 합계
        Map<String, BigDecimal> categoryMap = txList.stream()
                .collect(Collectors.groupingBy(
                        LedgerTransactionDTO::getCategoryName,
                        Collectors.reducing(BigDecimal.ZERO, LedgerTransactionDTO::getAmount, BigDecimal::add)
                ));

        // 카테고리 차트 JSON
        String categoryChart = buildCategoryChartJson(categoryMap, totalExpense);

        // 6개월 차트 JSON
        String sixMonthChart = buildSixMonthChartJson(userId, month);

        // 전월 리포트 비교
        YearMonth prevMonth = month.minusMonths(1);
        MonthReportDTO prev = monthReportMapper.findMonthReport(userId, prevMonth.toString());
        BigDecimal compareExpense = (prev != null) ? totalExpense.subtract(prev.getTotalExpense()) : BigDecimal.ZERO;

        // 저축액은 임시 고정값
        BigDecimal totalSaving = new BigDecimal("300000");
        BigDecimal compareSaving = (prev != null) ? totalSaving.subtract(prev.getTotalSaving()) : BigDecimal.ZERO;

        BigDecimal denominator = totalExpense.add(totalSaving);
        BigDecimal savingRate = (denominator.compareTo(BigDecimal.ZERO) == 0)
                ? BigDecimal.ZERO
                : totalSaving.divide(denominator, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        // 소비 성향 분석
        String feedback = generateFeedback(categoryMap, totalExpense);
        String topCategory = getTopCategory(categoryMap);
        String nextGoal = "다음 달 " + topCategory + " 지출 10% 줄이기";

        monthReportMapper.insertMonthReport(
                userId, monthStr, totalExpense, totalSaving, savingRate,
                compareExpense, compareSaving, categoryChart, sixMonthChart,
                feedback, nextGoal
        );
    }

    private String buildCategoryChartJson(Map<String, BigDecimal> categoryMap, BigDecimal total) {
        List<Map<String, Object>> chart = categoryMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> obj = new LinkedHashMap<>();
                    obj.put("category", e.getKey());
                    obj.put("amount", e.getValue());
                    obj.put("ratio", total.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO :
                            e.getValue().divide(total, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100))
                                    .setScale(1, RoundingMode.HALF_UP));
                    return obj;
                })
                .sorted((a, b) -> ((BigDecimal) b.get("amount")).compareTo((BigDecimal) a.get("amount")))
                .collect(Collectors.toList());

        return toJson(chart);
    }

    private String buildSixMonthChartJson(Long userId, YearMonth currentMonth) {
        List<MonthReportDTO> recent = monthReportMapper.findRecentMonthReportsInclusive(userId, currentMonth.toString(), 6);

        List<Map<String, Object>> chart = recent.stream()
                .sorted(Comparator.comparing(MonthReportDTO::getMonth))
                .map(r -> {
                    Map<String, Object> obj = new LinkedHashMap<>();
                    obj.put("month", r.getMonth());
                    obj.put("amount", r.getTotalExpense());
                    return obj;
                })
                .collect(Collectors.toList());

        return toJson(chart);
    }

    private String generateFeedback(Map<String, BigDecimal> categoryMap, BigDecimal totalExpense) {
        if (categoryMap.isEmpty()) return "소비 데이터가 부족합니다.";
        Map.Entry<String, BigDecimal> max = categoryMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow();
        BigDecimal ratio = max.getValue().divide(totalExpense, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        if (ratio.compareTo(BigDecimal.valueOf(40)) > 0) {
            return max.getKey() + " 지출이 많습니다. 절약이 필요해요.";
        }
        return "이번 달 소비 패턴이 안정적입니다.";
    }

    private String getTopCategory(Map<String, BigDecimal> categoryMap) {
        return categoryMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("전체");
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
