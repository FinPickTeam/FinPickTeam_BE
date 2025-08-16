package org.scoula.monthreport.service;

import org.scoula.monthreport.dto.AverageComparisonDto;
import org.scoula.monthreport.dto.PatternBannerDto;
import org.scoula.monthreport.enums.SpendingPatternType;

import java.util.*;
import java.util.stream.Collectors;

final class PatternBannerMapper {

    static PatternBannerDto toBanner(PatternClassification pc, AverageComparisonDto avg) {
        // 1) 메인 라벨: IMPULSE 우선, 없으면 overall
        SpendingPatternType primary = pc.getPatterns().contains(SpendingPatternType.IMPULSE)
                ? SpendingPatternType.IMPULSE : pc.getOverall();

        // 2) 보조 라벨: FOOD/Cafe 합쳐서 “외식 과다형” 우선, 없으면 첫 *_OVER
        Set<SpendingPatternType> sub = pc.getPatterns();
        boolean foodOver = sub.contains(SpendingPatternType.FOOD_OVER);
        boolean cafeOver = sub.contains(SpendingPatternType.CAFE_OVER);

        String secondaryCode = null, secondaryLabel = null;
        if (foodOver || cafeOver) {
            secondaryCode = "FOOD_CAFE_OVER";
            secondaryLabel = "외식 과다형";
        } else {
            Optional<SpendingPatternType> anyOver = sub.stream()
                    .filter(t -> t.name().endsWith("_OVER"))
                    .findFirst();
            if (anyOver.isPresent()) {
                secondaryCode = anyOver.get().name();
                secondaryLabel = anyOver.get().getLabel();
            }
        }

        // 3) 추천 카테고리: 서브 패턴 기반 → 없으면 평균 대비 초과(+) 상위 2개
        List<String> recCats = new ArrayList<>();
        if (foodOver) recCats.add("식비");
        if (cafeOver) recCats.add("카페");
        if (sub.contains(SpendingPatternType.SHOPPING_OVER)) recCats.add("쇼핑");
        if (sub.contains(SpendingPatternType.TRANSPORT_OVER)) recCats.add("교통");
        if (sub.contains(SpendingPatternType.SUBSCRIPTION_OVER)) recCats.add("구독");

        if (recCats.isEmpty() && avg != null && avg.byCategory != null) {
            // kosis key -> 한글 라벨
            Map<String, String> ko = Map.of(
                    "food","식비","cafe","카페","shopping","쇼핑",
                    "house","주거/통신","transport","교통","subscription","구독","mart","편의점/마트"
            );
            recCats = avg.byCategory.entrySet().stream()
                    .filter(e -> e.getValue() != null && e.getValue() > 0) // 평균보다 많이 쓴 카테고리
                    .sorted((a,b) -> Integer.compare(b.getValue(), a.getValue()))
                    .limit(2)
                    .map(e -> ko.getOrDefault(e.getKey(), e.getKey()))
                    .collect(Collectors.toList());
        }

        int recPct = recCats.size() >= 2 ? 15 : (recCats.isEmpty() ? 0 : 10);
        String catsText = joinWaGwa(recCats);
        String subtitle = recCats.isEmpty()
                ? "이번 달 소비는 전반적으로 안정적이에요."
                : String.format("다음 달엔 %s 지출을 약 %d%% 줄여보는 걸 추천드려요.", catsText, recPct);

        String headline = secondaryLabel == null
                ? primary.getLabel()
                : primary.getLabel() + " + " + secondaryLabel;

        return PatternBannerDto.builder()
                .headline(headline)
                .subtitle(subtitle)
                .primaryCode(primary.name())
                .primaryLabel(primary.getLabel())
                .secondaryCode(secondaryCode)
                .secondaryLabel(secondaryLabel)
                .recommendCategories(recCats)
                .recommendPercent(recPct == 0 ? null : recPct)
                .color("#5B5BD6")
                .icon("search")
                .build();
    }

    private static String joinWaGwa(List<String> items) {
        if (items == null || items.isEmpty()) return "";
        if (items.size() == 1) return items.get(0);
        String a = items.get(0), b = items.get(1);
        return a + (needsGwa(a) ? "과 " : "와 ") + b;
    }
    private static boolean needsGwa(String word) {
        if (word == null || word.isEmpty()) return false;
        char ch = word.charAt(word.length() - 1);
        if (ch < 0xAC00 || ch > 0xD7A3) return false; // 한글 아니면 대충 '와'
        return ((ch - 0xAC00) % 28) != 0; // 받침 있으면 '과'
    }
}
