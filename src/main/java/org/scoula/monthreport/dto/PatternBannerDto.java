package org.scoula.monthreport.dto;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PatternBannerDto {
    private String headline;                 // 감정적 소비형 + 외식 과다형
    private String subtitle;                 // 다음 달엔 식비와 카페 지출을 약 15% 줄여보는 걸 추천드려요.

    private String primaryCode;              // IMPULSE or FRUGAL/OVERSPENDER/...
    private String primaryLabel;             // 감정적 소비형 등

    private String secondaryCode;            // FOOD_CAFE_OVER or *_OVER
    private String secondaryLabel;           // 외식 과다형 등

    private List<String> recommendCategories; // ["식비","카페"]
    private Integer recommendPercent;        // 10 or 15

    // (선택) UI용
    private String color;    // "#5B5BD6"
    private String icon;     // "search"
}
