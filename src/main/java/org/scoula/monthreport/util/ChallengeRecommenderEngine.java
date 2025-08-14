package org.scoula.monthreport.util;

import org.scoula.monthreport.dto.RecommendationContext;
import org.scoula.monthreport.dto.RecommendedChallengeDto;
import org.scoula.monthreport.enums.SpendingPatternType;

import java.util.*;
import java.util.stream.Collectors;

public class ChallengeRecommenderEngine {

    // 기본 후보(부족할 때 채우는 안전판)
    private static final List<RecommendedChallengeDto> DEFAULTS = List.of(
            new RecommendedChallengeDto("식비 + 카페 지출 줄이기", "350,000원 이하로 유지해보세요."),
            new RecommendedChallengeDto("무지출 데이 도전!", "무지출 데이 2회 이상 가져보세요."),
            new RecommendedChallengeDto("주간 총지출 상한", "최근 3개월 평균 +10% 이내 유지")
    );

    public static List<RecommendedChallengeDto> recommend(RecommendationContext ctx) {
        if (ctx == null) return DEFAULTS.subList(0, 2);

        List<RecommendedChallengeDto> out = new ArrayList<>();

        // 거시 라벨 기반
        if (ctx.overall == SpendingPatternType.OVERSPENDER) {
            out.add(new RecommendedChallengeDto("주간 총지출 상한", "지난 3개월 평균 +10% 이내 유지"));
        }
        if (ctx.overall == SpendingPatternType.FRUGAL) {
            out.add(new RecommendedChallengeDto("저축률 35% 도전", "이번 달 저축률 +5% 올리기"));
        }

        // 행동/시간 패턴
        if (ctx.patterns != null && ctx.patterns.contains(SpendingPatternType.IMPULSE)) {
            out.add(new RecommendedChallengeDto("야간 결제 알림", "22~02시 결제 즉시 알림 켜기"));
        }
        if (ctx.patterns != null && ctx.patterns.contains(SpendingPatternType.WEEKEND)) {
            out.add(new RecommendedChallengeDto("주말 예산 봉투", "주말 총지출 2만 원 상한"));
        }

        // 카테고리 과다
        if (ctx.patterns != null && ctx.patterns.contains(SpendingPatternType.CAFE_OVER)) {
            out.add(new RecommendedChallengeDto("카페 주3회 이하", "평일 2회 + 주말 1회 제한"));
        }
        if (ctx.patterns != null && ctx.patterns.contains(SpendingPatternType.SUBSCRIPTION_OVER)) {
            out.add(new RecommendedChallengeDto("구독 1개 정리", "지난달 미사용 구독 해지"));
        }
        if (ctx.patterns != null && ctx.patterns.contains(SpendingPatternType.SHOPPING_OVER)) {
            out.add(new RecommendedChallengeDto("장바구니 24시간 룰", "결제 전 하루 뒤 다시 보기"));
        }

        // 벤치마크 초과(≥ +15%)
        if (ctx.averageDiffByCat != null) {
            ctx.averageDiffByCat.entrySet().stream()
                    .filter(e -> e.getValue() != null && e.getValue() >= 15)
                    .limit(1)
                    .forEach(e -> out.add(new RecommendedChallengeDto(
                            e.getKey() + " 15% 절감", "또래 평균 대비 초과분 줄이기")));
        }

        // ===== 중복 제거
        Map<String, RecommendedChallengeDto> dedup = out.stream()
                .collect(Collectors.toMap(RecommendedChallengeDto::getTitle, d -> d, (a, b) -> a, LinkedHashMap::new));
        List<RecommendedChallengeDto> result = new ArrayList<>(dedup.values());

        // ===== 최소 2개 보장: 부족하면 카테고리 비중 상위에서 1~2개 보충
        if (result.size() < 2) {
            pickByTopCategory(ctx, result);
        }
        // 여전히 2개 미만이면 DEFAULTS로 채우기
        if (result.size() < 2) {
            for (var d : DEFAULTS) {
                if (result.stream().noneMatch(x -> x.getTitle().equals(d.getTitle()))) {
                    result.add(d);
                }
                if (result.size() >= 2) break;
            }
        }

        // 최종: 정확히 2개만 반환
        return result.stream().limit(2).toList();
    }

    // 카테고리 비중 상위에서 빠르게 한 개 추천 뽑기
    private static void pickByTopCategory(RecommendationContext ctx, List<RecommendedChallengeDto> acc) {
        if (ctx.categoryRatios == null || ctx.categoryRatios.isEmpty()) return;
        var sorted = ctx.categoryRatios.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        for (String cat : sorted) {
            RecommendedChallengeDto c = null;
            switch (cat) {
                case "shopping" -> c = new RecommendedChallengeDto("장바구니 24시간 룰", "결제 전 하루 뒤 다시 보기");
                case "cafe"     -> c = new RecommendedChallengeDto("카페 주3회 이하", "평일 2회 + 주말 1회 제한");
                case "food"     -> c = new RecommendedChallengeDto("식비 주간예산제", "주간 예산 상한 설정하고 지키기");
                case "subscription" -> c = new RecommendedChallengeDto("구독 1개 정리", "지난달 미사용 구독 해지");
                case "transport" -> c = new RecommendedChallengeDto("이동비 10% 절감", "대중교통 정기권/합승/경로 최적화");
                default -> c = new RecommendedChallengeDto("주간 총지출 상한", "최근 3개월 평균 -10% 이내 유지");
            }
            String title = c.getTitle();
            if (acc.stream().noneMatch(x -> x.getTitle().equals(title))) {
                acc.add(c);
            }
            if (acc.size() >= 2) return;
        }
    }

    // 하위호환(기존 코드 호출부 유지) — 2개 반환
    public static List<RecommendedChallengeDto> recommend() {
        return DEFAULTS.subList(0, 2);
    }
}
