package org.scoula.monthreport.util;

import org.scoula.monthreport.dto.RecommendedChallengeDto;
import java.util.List;

public class ChallengeRecommenderEngine {
    public static List<RecommendedChallengeDto> recommend(/* 리포트 데이터 등 */) {
        // 예시: 식비/카페 지출 많은 경우에 맞는 챌린지 추천
        return List.of(
                new RecommendedChallengeDto("식비 + 카페 지출 줄이기", "350,000원 이하로 유지해보세요."),
                new RecommendedChallengeDto("무지출 데이 도전!", "무지출 데이 2회 이상 가져보세요.")
        );
    }
}
