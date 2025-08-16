package org.scoula.monthreport.util;

import org.scoula.transactions.domain.Ledger;
import org.scoula.monthreport.enums.SpendingPatternType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class SpendingAnalysisEngine {

    // 튜닝 가능한 임계치
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private static final BigDecimal FOOD_CAFE_RATIO_THRESHOLD = BigDecimal.valueOf(30); // %
    private static final BigDecimal MIDNIGHT_RATIO_THRESHOLD  = BigDecimal.valueOf(10); // %
    private static final BigDecimal WEEKEND_GAP_THRESHOLD     = BigDecimal.valueOf(20); // %p
    private static final BigDecimal SUBSCRIPTION_RATIO_THRESHOLD = BigDecimal.valueOf(15); // %

    // SpendingAnalysisEngine.analyze(...) 교체
    public static List<SpendingPatternType> analyze(List<Ledger> ledgerList) {
        if (ledgerList == null || ledgerList.isEmpty()) return List.of();

        List<Ledger> expenses = ledgerList.stream()
                .filter(l -> "EXPENSE".equalsIgnoreCase(safe(l.getType())))
                .collect(Collectors.toList());
        if (expenses.isEmpty()) return List.of();

        BigDecimal total = expenses.stream().map(l -> nvl(l.getAmount())).reduce(ZERO, BigDecimal::add);
        if (total.compareTo(ZERO) == 0) return List.of();

        // ★ 카테고리 정규화(공용 매퍼 사용)
        Map<String, BigDecimal> byCat = expenses.stream().collect(Collectors.groupingBy(
                l -> org.scoula.monthreport.util.CategoryMapper.toKosisKey(safe(l.getCategory())),
                Collectors.reducing(ZERO, l -> nvl(l.getAmount()), BigDecimal::add)
        ));

        BigDecimal foodCafe = byCat.getOrDefault("food", ZERO).add(byCat.getOrDefault("cafe", ZERO));
        BigDecimal subscription = byCat.getOrDefault("subscription", ZERO);

        BigDecimal foodCafeRatio = pct(foodCafe, total);
        BigDecimal subscriptionRatio = pct(subscription, total);

        // 시간/요일
        BigDecimal midnight = sumWhere(expenses, l -> {
            int h = l.getDate().getHour();
            return (h >= 22 || h < 2);
        });
        BigDecimal weekend = sumWhere(expenses, l -> {
            var d = l.getDate().getDayOfWeek();
            return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
        });
        BigDecimal weekday = total.subtract(weekend);

        BigDecimal midnightRatio = pct(midnight, total);
        BigDecimal weekendRatio  = pct(weekend, total);
        BigDecimal weekdayRatio  = pct(weekday, total);
        BigDecimal weekendGap    = weekendRatio.subtract(weekdayRatio); // %p

        List<SpendingPatternType> out = new ArrayList<>();

        if (foodCafeRatio.compareTo(FOOD_CAFE_RATIO_THRESHOLD) >= 0)
            out.add(SpendingPatternType.FOOD_OVER);

        if (subscriptionRatio.compareTo(SUBSCRIPTION_RATIO_THRESHOLD) >= 0)
            out.add(SpendingPatternType.SUBSCRIPTION_OVER);

        if (midnightRatio.compareTo(MIDNIGHT_RATIO_THRESHOLD) >= 0)
            out.add(SpendingPatternType.LATE_NIGHT);

        if (weekendGap.compareTo(WEEKEND_GAP_THRESHOLD) >= 0)
            out.add(SpendingPatternType.WEEKEND);

        // 야간/주말 과다면 충동적 소비 추정 라벨도 추가
        if (out.contains(SpendingPatternType.LATE_NIGHT) || out.contains(SpendingPatternType.WEEKEND))
            out.add(SpendingPatternType.IMPULSE);

        return Collections.unmodifiableList(out);
    }


    // ===== Helpers =====
    private static BigDecimal pct(BigDecimal part, BigDecimal total) {
        if (total.compareTo(ZERO) == 0) return ZERO;
        return part.multiply(HUNDRED).divide(total, 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal sumWhere(List<Ledger> list, java.util.function.Predicate<Ledger> p) {
        return list.stream().filter(p).map(l -> nvl(l.getAmount())).reduce(ZERO, BigDecimal::add);
    }

    private static String safe(String s) { return s == null ? "" : s; }
    private static BigDecimal nvl(BigDecimal v) { return v == null ? ZERO : v; }
}
