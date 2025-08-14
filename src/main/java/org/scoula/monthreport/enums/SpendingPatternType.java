package org.scoula.monthreport.enums;

import lombok.Getter;

@Getter
public enum SpendingPatternType {
    // 거시(Overall)
    FRUGAL("절약형"),
    STABLE("안정형"),
    OVERSPENDER("과소비형"),
    VOLATILE("변동형"),

    // 행동/시간
    IMPULSE("감정소비형"),
    HABIT("습관소비형"),
    LATE_NIGHT("심야지출형"),
    WEEKEND("주말집중형"),

    // 카테고리 과다
    FOOD_OVER("식비 과다형"),
    CAFE_OVER("간식 과다형"),
    SHOPPING_OVER("쇼핑 과다형"),
    HOUSE_OVER("주거/통신 과다형"),
    TRANSPORT_OVER("교통비 과다형"),
    SUBSCRIPTION_OVER("구독 과다형");

    private final String label;
    SpendingPatternType(String label) { this.label = label; }
}
