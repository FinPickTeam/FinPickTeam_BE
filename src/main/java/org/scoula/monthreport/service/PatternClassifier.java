package org.scoula.monthreport.service;

import org.scoula.monthreport.enums.SpendingPatternType;
import org.scoula.monthreport.util.SpendingAnalysisEngine;
import org.scoula.monthreport.util.CategoryMapper; // ★ 공용 매퍼 사용
import org.scoula.transactions.domain.Ledger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class PatternClassifier {

    // 임계치(필요하면 조절)
    private static final BigDecimal P10 = BigDecimal.valueOf(10);
    private static final BigDecimal P15 = BigDecimal.valueOf(15);
    private static final BigDecimal P20 = BigDecimal.valueOf(20);
    private static final BigDecimal P25 = BigDecimal.valueOf(25);
    private static final BigDecimal P30 = BigDecimal.valueOf(30);

    /**
     * @param savingRate 저축률(%) 0~100
     * @param last3moAvgExpense 직전 3개월 평균 지출 (현월 제외)
     * @param thisMonthExpense 이번달 지출
     * @param volatility 변동성 지표(표준편차, "금액" 단위)
     */
    public PatternClassification classify(List<Ledger> monthLedgers,
                                          BigDecimal savingRate,
                                          BigDecimal last3moAvgExpense,
                                          BigDecimal thisMonthExpense,
                                          BigDecimal volatility) {
        if (monthLedgers == null || monthLedgers.isEmpty()) {
            return new PatternClassification(SpendingPatternType.STABLE, Set.of());
        }

        // 1) 행동/시간 패턴 (엔진 결과 그대로 반영)
        var enginePatterns = SpendingAnalysisEngine.analyze(monthLedgers);
        boolean impulse = enginePatterns.contains(SpendingPatternType.IMPULSE);
        Set<SpendingPatternType> sub = new LinkedHashSet<>(enginePatterns); // ★ 중복 선언 금지

        // 2) 카테고리 합계 (DB 키/한글 라벨 섞여도 CategoryMapper로 정규화)
        Map<String, BigDecimal> byCat = monthLedgers.stream()
                .filter(l -> "EXPENSE".equalsIgnoreCase(safe(l.getType())))
                .map(l -> {
                    String key = CategoryMapper.fromAny(safe(l.getCategory())); // food/cafe/... 로 변환
                    return Map.entry(key, nvl(l.getAmount()));
                })
                .filter(e -> !CategoryMapper.excludeOnAnalysis(e.getKey())) // transfer/없음 제거
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.reducing(BigDecimal.ZERO, Map.Entry::getValue, BigDecimal::add)
                ));

        BigDecimal total = byCat.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.signum() == 0) {
            return new PatternClassification(SpendingPatternType.STABLE, sub);
        }
        var pct = (java.util.function.Function<BigDecimal, BigDecimal>)
                v -> v.multiply(BigDecimal.valueOf(100)).divide(total, 1, RoundingMode.HALF_UP);

        BigDecimal food      = byCat.getOrDefault("food", BigDecimal.ZERO);
        BigDecimal cafe      = byCat.getOrDefault("cafe", BigDecimal.ZERO);
        BigDecimal shopping  = byCat.getOrDefault("shopping", BigDecimal.ZERO);
        BigDecimal house     = byCat.getOrDefault("house", BigDecimal.ZERO); // finance는 매퍼가 house로 합침
        BigDecimal transport = byCat.getOrDefault("transport", BigDecimal.ZERO);
        BigDecimal subs      = byCat.getOrDefault("subscription", BigDecimal.ZERO);

        // 3) 서브 패턴 임계치 추가 (엔진 결과 + 카테고리 과다)
        if (impulse) sub.add(SpendingPatternType.IMPULSE);
        if (pct.apply(food).compareTo(P20) >= 0)       sub.add(SpendingPatternType.FOOD_OVER);
        if (pct.apply(cafe).compareTo(P15) >= 0)       sub.add(SpendingPatternType.CAFE_OVER);
        if (pct.apply(shopping).compareTo(P15) >= 0)   sub.add(SpendingPatternType.SHOPPING_OVER);
        if (pct.apply(house).compareTo(P20) >= 0)      sub.add(SpendingPatternType.HOUSE_OVER);
        if (pct.apply(transport).compareTo(P15) >= 0)  sub.add(SpendingPatternType.TRANSPORT_OVER);
        if (pct.apply(subs).compareTo(P10) >= 0)       sub.add(SpendingPatternType.SUBSCRIPTION_OVER);

        // 4) 거시 라벨
        SpendingPatternType overall = SpendingPatternType.STABLE;

        // 절약형
        if (savingRate != null && savingRate.compareTo(P30) >= 0) {
            overall = SpendingPatternType.FRUGAL;
        }

        // 과소비형
        if (last3moAvgExpense != null && thisMonthExpense != null && last3moAvgExpense.signum() > 0) {
            BigDecimal incPct = thisMonthExpense.subtract(last3moAvgExpense)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(last3moAvgExpense, 0, RoundingMode.HALF_UP);
            if (incPct.compareTo(BigDecimal.valueOf(20)) >= 0) {
                overall = SpendingPatternType.OVERSPENDER;
            }
        }

        // 변동형 (CV% = 표준편차 / 평균 × 100)
        if (volatility != null && last3moAvgExpense != null && last3moAvgExpense.signum() > 0) {
            BigDecimal cvPct = volatility.multiply(BigDecimal.valueOf(100))
                    .divide(last3moAvgExpense, 0, RoundingMode.HALF_UP);
            if (cvPct.compareTo(P25) >= 0 && overall == SpendingPatternType.STABLE) {
                overall = SpendingPatternType.VOLATILE;
            }
        }

        return new PatternClassification(overall, sub);
    }

    private static String safe(String s){ return s==null?"":s; }
    private static BigDecimal nvl(BigDecimal v){ return v==null? BigDecimal.ZERO : v; }
}
