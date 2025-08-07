package org.scoula.monthreport.enums;

public enum SpendingPatternType {
    // 기본 패턴
    SAVING("절약형"),
    STABLE("안정형"),
    EXCESS("과소비형"),
    FLUCTUATE("지출 변동형"),
    // 감정/행동 패턴
    IMPULSE("감정적 소비형"),
    HABITUAL("습관성 소비형"),
    REWARD("보상심리 소비형"),
    STRESS("스트레스 소비형"),
    LATE_NIGHT("야식/심야 소비형"),
    // 카테고리 과다형
    FOOD_OVER("외식 과다형"),
    CAFE_OVER("카페/간식 과다형"),
    SHOPPING_OVER("쇼핑/뷰티 과다형"),
    MART_OVER("마트/편의점 과다형"),
    HOBBY_OVER("취미/여가 과다형"),
    SUBSCRIPTION_OVER("구독서비스 과다형"),
    TRANSPORT_OVER("교통/차량 과다형"),
    FINANCE_OVER("금융/보험 고정지출형"),
    TRANSFER_OVER("이체/선물 과다형"),
    MEDICAL_OVER("의료/건강 지출형"),
    // 기간·특이 패턴
    SEASONAL("계절성 소비형"),
    EVENT("특정 이벤트/행사 소비형"),
    TRAVEL("여행/출장 지출형"),
    EDUCATION("교육/자기계발 소비형"),
    // 증가/감소 트렌드
    RAPID_INCREASE("지출 급증형"),
    RAPID_DECREASE("지출 급감형"),
    CONSISTENT("꾸준한 소비형"),
    RANDOM("불규칙 소비형"),
    // 반복/소액누적형
    REPEATED("반복 소액 지출형"),
    MICRO_PAYMENT("자잘한 소액 결제형"),
    // 기타
    ETC_OVER("기타 과다형"),
    NO_PATTERN("특징 없음");

    private final String label;

    SpendingPatternType(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }
}
