package org.scoula.monthreport.util;

import org.scoula.monthreport.dto.RecommendationContext;
import org.scoula.monthreport.dto.RecommendedChallengeDto;
import org.scoula.monthreport.enums.SpendingPatternType;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class ChallengeRecommenderEngine {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    public static List<RecommendedChallengeDto> recommend(RecommendationContext ctx) {
        // 컨텍스트 없으면 기본 두 개
        if (ctx == null || ctx.categoryRatios == null || ctx.categoryRatios.isEmpty()) {
            return List.of(
                    new RecommendedChallengeDto("총지출 10% 절감", "이번 달 대비 -10%"),
                    new RecommendedChallengeDto("식비·카페 10% 절감", "이번 달 대비 -10%")
            );
        }

        Map<String, BigDecimal> r = ctx.categoryRatios;                 // kosis key -> %
        Map<String, Integer> diff = ctx.averageDiffByCat == null ? Map.of() : ctx.averageDiffByCat;
        Set<SpendingPatternType> pats = ctx.patterns == null ? Set.of() : ctx.patterns;

        // 1) Overall 필요 여부
        boolean needOverall = ctx.overall == SpendingPatternType.OVERSPENDER
                || sumPositive(diff) >= 40; // 또래 평균 초과(+) 총합이 크면 전체 절감 유도

        List<RecommendedChallengeDto> out = new ArrayList<>();
        if (needOverall) {
            int pct = (ctx.overall == SpendingPatternType.OVERSPENDER) ? 12 : 10;
            out.add(new RecommendedChallengeDto(
                    "총지출 " + pct + "% 절감",
                    "이번 달 대비 -" + pct + "%"
            ));
        }

        // 2) 카테고리 그룹 점수 계산
        // 그룹 비중(%) 합
        Map<String, BigDecimal> gRatio = new LinkedHashMap<>();
        gRatio.put("food_cafe", r.getOrDefault("food", ZERO).add(r.getOrDefault("cafe", ZERO)));
        gRatio.put("shopping",  r.getOrDefault("shopping", ZERO));
        gRatio.put("house",     r.getOrDefault("house", ZERO));        // finance는 upstream에서 house로 합산된 전제
        gRatio.put("transport", r.getOrDefault("transport", ZERO));
        gRatio.put("subscription", r.getOrDefault("subscription", ZERO));
        gRatio.put("mart",      r.getOrDefault("mart", ZERO));

        // 그룹의 평균 대비 초과(+) 합
        Map<String, Integer> gDiff = new HashMap<>();
        gDiff.put("food_cafe", pos(diff.get("food")) + pos(diff.get("cafe")));
        gDiff.put("shopping",  pos(diff.get("shopping")));
        gDiff.put("house",     pos(diff.get("house")));
        gDiff.put("transport", pos(diff.get("transport")));
        gDiff.put("subscription", pos(diff.get("subscription")));
        gDiff.put("mart",      pos(diff.get("mart")));

        // 패턴 보너스 (있으면 가점)
        Map<String, Integer> bonus = new HashMap<>();
        bonus.put("food_cafe", 10 * (b(pats.contains(SpendingPatternType.FOOD_OVER)) + b(pats.contains(SpendingPatternType.CAFE_OVER))));
        bonus.put("shopping",  10 * b(pats.contains(SpendingPatternType.SHOPPING_OVER)));
        bonus.put("house",     10 * b(pats.contains(SpendingPatternType.HOUSE_OVER)));
        bonus.put("transport", 10 * b(pats.contains(SpendingPatternType.TRANSPORT_OVER)));
        bonus.put("subscription", 10 * b(pats.contains(SpendingPatternType.SUBSCRIPTION_OVER)));
        bonus.put("mart", 0);

        // 최종 점수: ratio(1.0) + over(1.5) + bonus
        record Item(String key, double score) {}
        List<Item> ranked = gRatio.entrySet().stream()
                .map(e -> {
                    String k = e.getKey();
                    double score = e.getValue().doubleValue()          // 비중
                            + 1.5 * gDiff.getOrDefault(k, 0)           // 평균 대비 초과(+)
                            + bonus.getOrDefault(k, 0);                 // 패턴 보너스
                    return new Item(k, score);
                })
                .sorted((a,b) -> Double.compare(b.score, a.score))
                .toList();

        // 3) 추천 선택: overall 있으면 카테고리 1개, 없으면 2개
        int needCats = needOverall ? 1 : 2;
        for (Item it : ranked) {
            if (needCats == 0) break;
            String k = it.key;
            BigDecimal ratio = gRatio.getOrDefault(k, ZERO);
            int over = gDiff.getOrDefault(k, 0);
            int bon = bonus.getOrDefault(k, 0);
            int cut = calcCut(ratio, over, bon); // 10/12/15%

            if (cut <= 0) continue; // 의미 없는 추천 스킵
            out.add(new RecommendedChallengeDto(
                    displayName(k) + " " + cut + "% 절감",
                    "이번 달 대비 -" + cut + "%"
            ));
            needCats--;
        }

        // 4) 보정: 혹시 2개 못 채우면 기본으로 채움
        while (out.size() < 2) {
            out.add(new RecommendedChallengeDto("총지출 10% 절감", "이번 달 대비 -10%"));
        }

        // 끝: 정확히 2개
        return out.stream().limit(2)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // ===== helpers =====
    private static int sumPositive(Map<String, Integer> m) {
        if (m == null || m.isEmpty()) return 0;
        int s = 0;
        for (Integer v : m.values()) if (v != null && v > 0) s += v;
        return s;
    }
    private static int pos(Integer v) { return (v == null || v < 0) ? 0 : v; }
    private static int b(boolean v) { return v ? 1 : 0; }

    private static int calcCut(BigDecimal ratioPct, int overPct, int bonus) {
        // 세기 판단: 평균보다 많이 썼거나 비중이 큰 카테고리는 더 크게 깎자
        if (overPct >= 30 || ratioPct.compareTo(new BigDecimal("25.0")) >= 0 || bonus >= 10) return 15;
        if (overPct >= 15 || ratioPct.compareTo(new BigDecimal("20.0")) >= 0) return 12;
        if (ratioPct.compareTo(new BigDecimal("8.0")) >= 0) return 10;  // 너무 작은 비중은 패스
        return 0;
    }

    private static String displayName(String key) {
        return switch (key) {
            case "food_cafe"   -> "식비·카페";
            case "shopping"    -> "쇼핑";
            case "house"       -> "주거/통신(고정비)";
            case "transport"   -> "교통";
            case "subscription"-> "구독";
            case "mart"        -> "편의·마트";
            default            -> "기타";
        };
    }

    // 하위호환: 옛 호출부
    public static List<RecommendedChallengeDto> recommend() {
        return List.of(
                new RecommendedChallengeDto("총지출 10% 절감", "이번 달 대비 -10%"),
                new RecommendedChallengeDto("식비·카페 10% 절감", "이번 달 대비 -10%")
        );
    }
}
