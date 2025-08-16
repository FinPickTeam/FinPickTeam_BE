package org.scoula.monthreport.service;

import org.scoula.monthreport.enums.SpendingPatternType;
import org.scoula.monthreport.util.SpendingAnalysisEngine;
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
     * @param volatility 변동성 지표(표준편차, 통화 단위 or %, 내부 로직은 %로 쓰지 않음)
     */
    public PatternClassification classify(List<Ledger> monthLedgers,
                                          BigDecimal savingRate,
                                          BigDecimal last3moAvgExpense,
                                          BigDecimal thisMonthExpense,
                                          BigDecimal volatility) {
        if (monthLedgers == null || monthLedgers.isEmpty()) {
            return new PatternClassification(SpendingPatternType.STABLE, Set.of());
        }

        // 1) 행동 패턴(예: 충동구매) – 기존 엔진
        var engine = SpendingAnalysisEngine.analyze(monthLedgers);
        boolean impulse = engine.contains(SpendingPatternType.IMPULSE);

        // 2) 카테고리 합계(정규화 키로 집계)
        Map<String, BigDecimal> byCat = monthLedgers.stream()
                .filter(l -> "EXPENSE".equalsIgnoreCase(l.getType()))
                .collect(Collectors.groupingBy(
                        l -> mapToKosisKey(safe(l.getCategory())),
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

        // 3) 서브 패턴
        Set<SpendingPatternType> sub = new LinkedHashSet<>();
        if (impulse) sub.add(SpendingPatternType.IMPULSE);
        if (pct.apply(food).compareTo(P20) >= 0) sub.add(SpendingPatternType.FOOD_OVER);
        if (pct.apply(cafe).compareTo(P15) >= 0) sub.add(SpendingPatternType.CAFE_OVER);
        if (pct.apply(shopping).compareTo(P15) >= 0) sub.add(SpendingPatternType.SHOPPING_OVER);
        if (pct.apply(house).compareTo(P20) >= 0) sub.add(SpendingPatternType.HOUSE_OVER);
        if (pct.apply(transport).compareTo(P15) >= 0) sub.add(SpendingPatternType.TRANSPORT_OVER);
        if (pct.apply(subs).compareTo(P10) >= 0) sub.add(SpendingPatternType.SUBSCRIPTION_OVER);

        // 4) 거시 라벨
        SpendingPatternType overall = SpendingPatternType.STABLE;
        if (savingRate != null && savingRate.compareTo(P30) >= 0) {
            overall = SpendingPatternType.FRUGAL;
        }

        if (last3moAvgExpense != null && last3moAvgExpense.signum() > 0) {
            BigDecimal incPct = thisMonthExpense.subtract(last3moAvgExpense)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(last3moAvgExpense, 0, RoundingMode.HALF_UP);
            if (incPct.compareTo(BigDecimal.valueOf(20)) >= 0) {
                overall = SpendingPatternType.OVERSPENDER;
            }
        }

        // 변동성 지표가 큰데 아직도 STABLE이면 VOLATILE로 승격
        // (volatility가 금액 표준편차라면, 평균 대비 25%↑ 같은 %기준이 더 안정적이나,
        //  여기서는 간단히 절대적 문턱값을 적용하거나 엔진 내부에서 %를 계산하도록 확장 가능)
        if (volatility != null && volatility.compareTo(P25) >= 0) {
            if (overall == SpendingPatternType.STABLE) overall = SpendingPatternType.VOLATILE;
        }

        return new PatternClassification(overall, sub);
    }

    private static String safe(String s){ return s==null?"":s; }

    /** 한글 라벨을 KOSIS 키로 맵핑 */
    private static String mapToKosisKey(String label) {
        if (label == null) return "etc";
        String kk = label.trim();
        if (kk.contains("식비")) return "food";
        if (kk.contains("카페") || kk.contains("간식")) return "cafe";
        if (kk.contains("쇼핑") || kk.contains("미용")) return "shopping";
        if (kk.contains("편의점") || kk.contains("마트") || kk.contains("잡화")) return "mart";
        if (kk.contains("주거") || kk.contains("통신") || kk.contains("보험")) return "house";
        if (kk.contains("교통") || kk.contains("자동차")) return "transport";
        if (kk.contains("구독")) return "subscription";
        return "etc";
    }
}
