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

    public static List<SpendingPatternType> analyze(List<Ledger> ledgerList) {
        if (ledgerList == null || ledgerList.isEmpty()) {
            return List.of();
        }

        // 1) EXPENSE만 분석
        List<Ledger> expenses = ledgerList.stream()
                .filter(l -> "EXPENSE".equalsIgnoreCase(safe(l.getType())))
                .collect(Collectors.toList());
        if (expenses.isEmpty()) return List.of();

        BigDecimal total = expenses.stream()
                .map(l -> nvl(l.getAmount()))
                .reduce(ZERO, BigDecimal::add);

        if (total.compareTo(ZERO) == 0) return List.of();

        // 2) 카테고리 합계
        Map<String, BigDecimal> byCategory = expenses.stream().collect(Collectors.groupingBy(
                l -> safe(l.getCategory()), Collectors.reducing(ZERO, l -> nvl(l.getAmount()), BigDecimal::add)
        ));

        BigDecimal foodCafe = byCategory.getOrDefault("food", ZERO)
                .add(byCategory.getOrDefault("cafe", ZERO));
        BigDecimal subscription = byCategory.getOrDefault("subscription", ZERO);

        BigDecimal foodCafeRatio = pct(foodCafe, total);
        BigDecimal subscriptionRatio = pct(subscription, total);

        // 3) 시간/요일 패턴
        BigDecimal midnight = sumWhere(expenses, l -> {
            LocalDateTime dt = l.getDate();
            int h = dt.getHour();
            return (h >= 22 || h < 2);
        });
        BigDecimal weekend = sumWhere(expenses, l -> {
            DayOfWeek d = l.getDate().getDayOfWeek();
            return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
        });
        BigDecimal weekday = total.subtract(weekend);

        BigDecimal midnightRatio = pct(midnight, total);
        BigDecimal weekendRatio  = pct(weekend, total);
        BigDecimal weekdayRatio  = pct(weekday, total);
        BigDecimal weekendGap    = weekendRatio.subtract(weekdayRatio);

        // 4) 패턴 판단
        List<SpendingPatternType> out = new ArrayList<>();

        if (foodCafeRatio.compareTo(FOOD_CAFE_RATIO_THRESHOLD) >= 0) {
            out.add(SpendingPatternType.FOOD_OVER);
        }

        boolean impulse = false;
        if (midnightRatio.compareTo(MIDNIGHT_RATIO_THRESHOLD) >= 0) impulse = true;
        if (weekendGap.compareTo(WEEKEND_GAP_THRESHOLD) >= 0) impulse = true;
        if (subscriptionRatio.compareTo(SUBSCRIPTION_RATIO_THRESHOLD) >= 0) impulse = true; // 고정비 과다도 충동/방임으로 판단

        if (impulse) out.add(SpendingPatternType.IMPULSE);

        return Collections.unmodifiableList(out);
    }

    // 기존 시그니처 유지(백워드 호환)
    public static String getPatternFeedback(List<SpendingPatternType> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return "소비 데이터가 부족합니다.";
        }
        boolean hasImpulse = patterns.contains(SpendingPatternType.IMPULSE);
        boolean hasFood    = patterns.contains(SpendingPatternType.FOOD_OVER);

        if (hasImpulse && hasFood) {
            return "감정적 소비와 외식/카페 지출이 높습니다. 야간·주말 결제 제한과 식비 예산(주 단위)을 함께 설정해 보세요.";
        } else if (hasImpulse) {
            return "감정적 소비가 포착됩니다. 야간 결제 알림/한도와 주말 예산 상한을 설정해 보세요.";
        } else if (hasFood) {
            return "외식·카페 비중이 높습니다. 평일 도시락/홈카페 비율을 늘려 다음 달 식비를 15% 줄여보세요.";
        }
        return "이번 달 소비 패턴이 안정적입니다.";
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
