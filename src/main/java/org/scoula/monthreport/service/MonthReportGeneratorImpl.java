package org.scoula.monthreport.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.scoula.monthreport.dto.CardTransactionDTO;
import org.scoula.monthreport.dto.MonthReportDTO;
import org.scoula.monthreport.mapper.MonthReportMapper;
import org.scoula.monthreport.util.CategoryMapper;
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

        List<CardTransactionDTO> txList = monthReportMapper.findCardTransactions(userId, from, to);
        if (txList.isEmpty()) return;

        BigDecimal totalExpense = txList.stream()
                .map(CardTransactionDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> categoryMap = txList.stream()
                .collect(Collectors.groupingBy(
                        tx -> CategoryMapper.map(tx.getMerchantIndustryCode()),
                        Collectors.reducing(BigDecimal.ZERO, CardTransactionDTO::getAmount, BigDecimal::add)
                ));

        String categoryChart = buildCategoryChartJson(txList);
        Map<String, BigDecimal> sixMonthMap = getSixMonthTrend(userId, month);
        String sixMonthChart = buildSixMonthChartJson(userId, month);

        YearMonth prevMonth = month.minusMonths(1);
        MonthReportDTO prev = monthReportMapper.findMonthReport(userId, prevMonth.toString());
        BigDecimal compareExpense = (prev != null) ? totalExpense.subtract(prev.getTotalExpense()) : BigDecimal.ZERO;
        BigDecimal totalSaving = new BigDecimal("300000"); // TODO: 실제 소득 계산 로직 필요
        BigDecimal compareSaving = (prev != null) ? totalSaving.subtract(prev.getTotalSaving()) : BigDecimal.ZERO;

        BigDecimal denominator = totalExpense.add(totalSaving);
        BigDecimal savingRate = denominator.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : totalSaving.divide(denominator, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));


        String feedback = generateFeedback(categoryMap, totalExpense);
        String nextGoal = "다음 달 " + topCategory(categoryMap) + " 지출 10% 줄이기";

        monthReportMapper.insertMonthReport(userId, monthStr, totalExpense, totalSaving, savingRate,
                compareExpense, compareSaving, categoryChart, sixMonthChart, feedback, nextGoal);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Map<String, BigDecimal> getSixMonthTrend(Long userId, YearMonth currentMonth) {
        List<MonthReportDTO> recent = monthReportMapper.findRecentMonthReports(userId, currentMonth.toString(), 6);
        return recent.stream().collect(Collectors.toMap(
                MonthReportDTO::getMonth,
                MonthReportDTO::getTotalExpense
        ));
    }

    private String generateFeedback(Map<String, BigDecimal> categoryMap, BigDecimal totalExpense) {
        if (categoryMap.isEmpty()) return "소비 데이터가 부족합니다.";
        Map.Entry<String, BigDecimal> max = categoryMap.entrySet().stream()
                .max(Comparator.comparing(Map.Entry::getValue))
                .orElseThrow();
        BigDecimal ratio = max.getValue().divide(totalExpense, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        if (ratio.compareTo(BigDecimal.valueOf(40)) > 0) {
            return max.getKey() + " 지출이 많습니다. 절약이 필요해요.";
        }
        return "이번 달 소비 패턴이 안정적입니다.";
    }

    private String topCategory(Map<String, BigDecimal> categoryMap) {
        return categoryMap.entrySet().stream()
                .max(Comparator.comparing(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse("전체");
    }

    private String buildCategoryChartJson(List<CardTransactionDTO> txList) {
        BigDecimal total = txList.stream()
                .map(CardTransactionDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> categorySum = txList.stream()
                .collect(Collectors.groupingBy(
                        tx -> CategoryMapper.map(tx.getMerchantIndustryCode()),
                        Collectors.reducing(BigDecimal.ZERO, CardTransactionDTO::getAmount, BigDecimal::add)
                ));

        List<Map<String, Object>> result = categorySum.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> obj = new LinkedHashMap<>();
                    obj.put("category", entry.getKey());
                    obj.put("amount", entry.getValue());
                    obj.put("ratio", entry.getValue()
                            .divide(total, 4, BigDecimal.ROUND_HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(1, BigDecimal.ROUND_HALF_UP));
                    return obj;
                })
                .sorted((a, b) -> ((BigDecimal) b.get("amount")).compareTo((BigDecimal) a.get("amount")))
                .collect(Collectors.toList());

        return toJson(result);
    }

    private String buildSixMonthChartJson(Long userId, YearMonth currentMonth) {
        List<MonthReportDTO> recent = monthReportMapper.findRecentMonthReportsInclusive(userId, currentMonth.toString(), 6);

        // 가장 오래된 월부터 정렬
        List<Map<String, Object>> list = recent.stream()
                .sorted(Comparator.comparing(MonthReportDTO::getMonth))
                .map(dto -> {
                    Map<String, Object> obj = new LinkedHashMap<>();
                    obj.put("month", dto.getMonth());
                    obj.put("amount", dto.getTotalExpense());
                    return obj;
                })
                .collect(Collectors.toList());

        return toJson(list);
    }


}