
package org.scoula.survey.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.survey.domain.SurveyVO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyDTO {
    private Long id; // user.id와 동일하며, investment_types 테이블의 primary key
    private int totalScore;
    private String propensityType;
    private String question1;
    private String question2;
    private String question3;
    private String question4;

    public SurveyVO toVO(){
        SurveyVO surveyVO = new SurveyVO();
        surveyVO.setId(id);
        surveyVO.setTotalScore(totalScore);
        surveyVO.setPropensityType(propensityType);
        surveyVO.setQuestion1(question1);
        surveyVO.setQuestion2(question2);
        surveyVO.setQuestion3(question3);
        surveyVO.setQuestion4(question4);
        return surveyVO;
    }

    public static SurveyDTO of(SurveyVO surveyVO){

        return SurveyDTO.builder() // 클래스 이름으로 빌더를 호출합니다.
                .id(surveyVO.getId())
                .totalScore(surveyVO.getTotalScore())
                .propensityType(surveyVO.getPropensityType())
                .question1(surveyVO.getQuestion1())
                .question2(surveyVO.getQuestion2())
                .question3(surveyVO.getQuestion3())
                .question4(surveyVO.getQuestion4())
                .build(); // 빌더로 생성된 객체를 즉시 반환합니다.
    }

}


