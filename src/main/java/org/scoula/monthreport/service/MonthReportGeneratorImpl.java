package org.scoula.monthreport.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.scoula.account.domain.Account;
import org.scoula.account.mapper.AccountMapper;
import org.scoula.card.domain.Card;
import org.scoula.card.mapper.CardMapper;
import org.scoula.monthreport.domain.LedgerTransaction;
import org.scoula.monthreport.domain.MonthReport;
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
    private final AccountMapper accountMapper;
    private final CardMapper cardMapper;

    @Override
    public void generate(Long userId, String monthStr) {
        YearMonth month = YearMonth.parse(monthStr);
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        List<LedgerTransaction> txList = monthReportMapper.findLedgerTransactions(userId, from, to);
        if (txList.isEmpty()) return;

        // 해당 month가지 결제된 계좌/카드 ID 수집
        Set<Long> accountIds = txList.stream()
                .filter(tx -> "ACCOUNT".equals(tx.getSourceType()) && tx.getAccountId() != null)
                .map(LedgerTransaction::getAccountId)
                .collect(Collectors.toSet());

        Set<Long> cardIds = txList.stream()
                .filter(tx -> "CARD".equals(tx.getSourceType()) && tx.getCardId() != null)
                .map(LedgerTransaction::getCardId)
                .collect(Collectors.toSet());

        Map<Long, Account> accountMap = accountIds.isEmpty() ? Collections.emptyMap() :
                accountMapper.findByIdList(new ArrayList<>(accountIds)).stream()
                        .collect(Collectors.toMap(Account::getId, a -> a));

        Map<Long, Card> cardMap = cardIds.isEmpty() ? Collections.emptyMap() :
                cardMapper.findByIdList(new ArrayList<>(cardIds)).stream()
                        .collect(Collectors.toMap(Card::getId, c -> c));

        List<LedgerTransaction> filteredTxList = txList.stream()
                .filter(tx -> {
                    if ("ACCOUNT".equals(tx.getSourceType()) && tx.getAccountId() != null) {
                        Account acc = accountMap.get(tx.getAccountId());
                        return acc != null && Boolean.TRUE.equals(acc.getIsActive());
                    }
                    if ("CARD".equals(tx.getSourceType()) && tx.getCardId() != null) {
                        Card card = cardMap.get(tx.getCardId());
                        return card != null && Boolean.TRUE.equals(card.getIsActive());
                    }
                    return false;
                })
                .toList();

        if (filteredTxList.isEmpty()) return;

        BigDecimal totalExpense = filteredTxList.stream()
                .map(LedgerTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> categoryMap = filteredTxList.stream()
                .collect(Collectors.groupingBy(
                        LedgerTransaction::getCategoryName,
                        Collectors.reducing(BigDecimal.ZERO, LedgerTransaction::getAmount, BigDecimal::add)
                ));

        String categoryChart = buildCategoryChartJson(categoryMap, totalExpense);
        String sixMonthChart = buildSixMonthChartJson(userId, month);

        YearMonth prevMonth = month.minusMonths(1);
        MonthReport prev = monthReportMapper.findMonthReport(userId, prevMonth.toString());

        BigDecimal compareExpense = (prev != null) ? totalExpense.subtract(prev.getTotalExpense()) : BigDecimal.ZERO;
        BigDecimal totalSaving = new BigDecimal("300000"); // FIXME: 동적으로 설정할 것
        BigDecimal compareSaving = (prev != null) ? totalSaving.subtract(prev.getTotalSaving()) : BigDecimal.ZERO;

        BigDecimal denominator = totalExpense.add(totalSaving);
        BigDecimal savingRate = (denominator.compareTo(BigDecimal.ZERO) == 0)
                ? BigDecimal.ZERO
                : totalSaving.divide(denominator, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

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
        List<MonthReport> recent = monthReportMapper.findRecentMonthReportsInclusive(userId, currentMonth.toString(), 6);

        List<Map<String, Object>> chart = recent.stream()
                .sorted(Comparator.comparing(MonthReport::getMonth))
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
