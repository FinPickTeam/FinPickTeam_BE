package org.scoula.monthreport.util;

import org.scoula.transactions.domain.Ledger;
import org.scoula.monthreport.enums.SpendingPatternType;
import java.util.List;

public class SpendingAnalysisEngine {
    public static List<SpendingPatternType> analyze(List<Ledger> ledgerList) {
        // 예시: 카페/식비 비율 30%↑면 패턴 추가
        // 실전에서는 카테고리 비율, 금액, 트렌드 등으로 분기
        return List.of(SpendingPatternType.IMPULSE, SpendingPatternType.FOOD_OVER);
    }

    public static String getPatternFeedback(List<SpendingPatternType> patterns) {
        // 패턴 조합에 따른 긴 문장 리턴(월간 피드백)
        return "감정적 소비와 외식 지출이 두드러집니다. 다음 달엔 식비와 카페 지출을 15% 줄여보세요.";
    }
}
