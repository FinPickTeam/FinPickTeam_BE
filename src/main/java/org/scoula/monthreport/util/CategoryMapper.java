package org.scoula.monthreport.util;

public final class CategoryMapper {
    private CategoryMapper(){}

    // DB name(영문 키) -> KOSIS 키
    public static String toKosisKey(String name){
        if (name == null) return "etc";
        return switch (name.trim()) {
            case "food"         -> "food";
            case "cafe"         -> "cafe";
            case "shopping"     -> "shopping";
            case "mart"         -> "mart";
            case "house"        -> "house";
            case "hobby"        -> "etc";        // 취미/여가는 벤치마크엔 없으니 etc로 귀속
            case "transport"    -> "transport";
            case "finance"      -> "house";      // 보험 등 금융 고정비는 주거/통신에 합산
            case "subscription" -> "subscription";
            case "transfer"     -> "etc";        // 분석/차트에서는 보통 제외(아래 exclude 함수 참고)
            case "etc", "uncategorized" -> "etc";
            default -> "etc";
        };
    }

    // 라벨(한글) 또는 애매한 문자열 -> KOSIS 키 (백업용)
    public static String fromAny(String labelOrName){
        if (labelOrName == null) return "etc";
        String s = labelOrName.trim();
        // DB 키가 그대로 들어오는 경우
        switch (s) {
            case "food","cafe","shopping","mart","house","hobby","transport","finance","subscription","transfer","etc","uncategorized":
                return toKosisKey(s);
        }
        // 한글 라벨 추정
        if (s.contains("식비")) return "food";
        if (s.contains("카페") || s.contains("간식")) return "cafe";
        if (s.contains("쇼핑") || s.contains("미용")) return "shopping";
        if (s.contains("편의점") || s.contains("마트") || s.contains("잡화")) return "mart";
        if (s.contains("주거") || s.contains("통신")) return "house";
        if (s.contains("교통") || s.contains("자동차")) return "transport";
        if (s.contains("보험") || s.contains("금융")) return "house"; // finance 라벨도 house로
        if (s.contains("구독")) return "subscription";
        if (s.contains("취미") || s.contains("여가")) return "etc";
        if (s.contains("이체")) return "etc";
        if (s.contains("없음")) return "etc";
        return "etc";
    }

    // 차트에서 빼고 싶은 카테고리 (이체/없음)
    public static boolean excludeOnCharts(String nameOrLabel){
        if (nameOrLabel == null) return true;
        String s = nameOrLabel.trim();
        return s.equals("transfer") || s.equals("uncategorized") || s.equals("카테고리 없음");
    }

    // 분석에서 무시하고 싶은 카테고리(안전망: 이체/없음)
    public static boolean excludeOnAnalysis(String nameOrLabel){
        return excludeOnCharts(nameOrLabel);
    }
}
