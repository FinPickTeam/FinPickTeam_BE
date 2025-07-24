package org.scoula.survey.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyVO {
    private Long id; // user의 id
    private Integer totalScore; // db 테이블의 total_score 컬럼 (mapUnderscoreToCamelCase로 매핑)
    private String propensityType;
    private String question1;
    private String question2;
    private String question3;
    private String question4;
}