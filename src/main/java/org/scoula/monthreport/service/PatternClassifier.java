package org.scoula.monthreport.service;

import org.scoula.monthreport.enums.SpendingPatternType;
import org.scoula.monthreport.util.SpendingAnalysisEngine;
import org.scoula.transactions.domain.Ledger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class PatternClassifier {

    // 튜닝 임계치
    private static final BigDecimal P10 = BigDecimal.valueOf(10);
    private static final BigDecimal P15 = BigDecimal.valueOf(15);
    private static final BigDecimal P20 = BigDecimal.valueOf(20);
    private static final BigDecimal P30 = BigDecimal.valueOf(30);

    /**
     * @param savingRate 저축률(%) 0~100
     * @param last3moAvgExpense 직전 3개월 평균 지출
     * @param thisMonthExpense 이번달 지출
     * @param volatility 변동성 지표(선택, %)
     */
    public PatternClassification classify(List<Ledger> monthLedgers,
                                          BigDecimal savingRate,
                                          BigDecimal last3moAvgExpense,
                                          BigDecimal thisMonthExpense,
                                          BigDecimal volatility) {
        if (monthLedgers == null || monthLedgers.isEmpty()) {
            return new PatternClassification(SpendingPatternType.STABLE, Set.of());
        }

        // 1) 기존 엔진 활용(IMPULSE 등)
        var engine = SpendingAnalysisEngine.analyze(monthLedgers);
        boolean impulse = engine.contains(SpendingPatternType.IMPULSE);

        // 2) 카테고리 합계/비중
        Map<String, BigDecimal> byCat = monthLedgers.stream()
                .filter(l -> "EXPENSE".equalsIgnoreCase(l.getType()))
                .collect(Collectors.groupingBy(
                        l -> safe(l.getCategory()),
                        Collectors.reducing(BigDecimal.ZERO, Ledger::getAmount, BigDecimal::add)
                ));

        BigDecimal total = byCat.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.signum() == 0) {
            return new PatternClassification(SpendingPatternType.STABLE, Set.of());
        }
        java.util.function.Function<BigDecimal, BigDecimal> pct = v ->
                v.multiply(BigDecimal.valueOf(100)).divide(total, 1, RoundingMode.HALF_UP);

        BigDecimal food = byCat.getOrDefault("food", BigDecimal.ZERO);
        BigDecimal cafe = byCat.getOrDefault("cafe", BigDecimal.ZERO);
        BigDecimal shopping = byCat.getOrDefault("shopping", BigDecimal.ZERO);
        BigDecimal house = byCat.getOrDefault("house", BigDecimal.ZERO).add(byCat.getOrDefault("finance", BigDecimal.ZERO));
        BigDecimal transport = byCat.getOrDefault("transport", BigDecimal.ZERO);
        BigDecimal subs = byCat.getOrDefault("subscription", BigDecimal.ZERO);

        // 3) 서브 패턴 수집
        Set<SpendingPatternType> sub = new LinkedHashSet<>();
        if (impulse) sub.add(SpendingPatternType.IMPULSE);
        if (pct.apply(food).compareTo(P20) >= 0) sub.add(SpendingPatternType.FOOD_OVER);
        if (pct.apply(cafe).compareTo(P15) >= 0) sub.add(SpendingPatternType.CAFE_OVER);
        if (pct.apply(shopping).compareTo(P15) >= 0) sub.add(SpendingPatternType.SHOPPING_OVER);
        if (pct.apply(house).compareTo(P20) >= 0) sub.add(SpendingPatternType.HOUSE_OVER);
        if (pct.apply(transport).compareTo(P15) >= 0) sub.add(SpendingPatternType.TRANSPORT_OVER);
        if (pct.apply(subs).compareTo(P10) >= 0) sub.add(SpendingPatternType.SUBSCRIPTION_OVER);

        // 4) 거시 라벨 결정
        SpendingPatternType overall = SpendingPatternType.STABLE;
        if (savingRate != null && savingRate.compareTo(P30) >= 0) overall = SpendingPatternType.FRUGAL;

        if (last3moAvgExpense != null && last3moAvgExpense.signum() > 0) {
            BigDecimal incPct = thisMonthExpense.subtract(last3moAvgExpense)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(last3moAvgExpense, 0, RoundingMode.HALF_UP);
            if (incPct.compareTo(BigDecimal.valueOf(20)) >= 0) {
                overall = SpendingPatternType.OVERSPENDER;
            }
        }
        if (volatility != null && volatility.compareTo(BigDecimal.valueOf(25)) >= 0) {
            if (overall == SpendingPatternType.STABLE) overall = SpendingPatternType.VOLATILE;
        }
        return new PatternClassification(overall, sub);
    }

    private static String safe(String s){ return s==null?"":s; }
}
