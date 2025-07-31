package org.scoula.monthreport.util;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum CategoryMapper {

    FOOD("5611", "식비"),
    CAFE("5814", "카페/간식"),
    BEAUTY("7230", "쇼핑/미용"),
    MART("5411", "편의점/마트/잡화"),
    TRANSPORT("4111", "교통/자동차"),
    LIVING("4812", "주거/통신"),
    HOBBY("7999", "취미/여가/구독"),
    ETC("9999", "기타 금융");


    private final String code;
    private final String category;

    CategoryMapper(String code, String category) {
        this.code = code;
        this.category = category;
    }

    public static String map(String industryCode) {
        return Arrays.stream(values())
                .filter(e -> e.code.equalsIgnoreCase(industryCode))
                .map(CategoryMapper::getCategory)
                .findFirst()
                .orElse("기타 금융");
    }
}
