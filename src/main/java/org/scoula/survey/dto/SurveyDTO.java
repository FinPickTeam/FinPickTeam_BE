package org.scoula.survey.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.survey.domain.SurveyVO; // SurveyVO 임포트

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data // Getter, Setter, equals, hashCode, toString 자동 생성
@NoArgsConstructor // 기본 생성자 자동 생성
@AllArgsConstructor // 모든 필드를 포함하는 생성자 자동 생성
@Builder // 빌더 패턴 사용
public class SurveyDTO {
    private Long id; // user의 id
    private Integer totalScore;
    private String propensityType;
    private String question1;
    private String question2;
    private String question3;
    private String question4;
    private String question5;
    private String question6;
    private String question7;
    private String question8;
    private String question9;
    private String question10;

    public static SurveyDTO of(SurveyVO surveyVO) {
        return SurveyDTO.builder()
                .id(surveyVO.getId())
                .totalScore(surveyVO.getTotalScore())
                .propensityType(surveyVO.getPropensityType())
                .question1(surveyVO.getQuestion1())
                .question2(surveyVO.getQuestion2())
                .question3(surveyVO.getQuestion3())
                .question4(surveyVO.getQuestion4())
                .question5(surveyVO.getQuestion5())
                .question6(surveyVO.getQuestion6())
                .question7(surveyVO.getQuestion7())
                .question8(surveyVO.getQuestion8())
                .question9(surveyVO.getQuestion9())
                .question10(surveyVO.getQuestion10())
                .build();
    }

    public SurveyVO toVO() {
        SurveyVO surveyVO = new SurveyVO();
        surveyVO.setId(this.id);
        surveyVO.setTotalScore(this.totalScore);
        surveyVO.setPropensityType(this.propensityType);
        surveyVO.setQuestion1(this.question1);
        surveyVO.setQuestion2(this.question2);
        surveyVO.setQuestion3(this.question3);
        surveyVO.setQuestion4(this.question4);
        surveyVO.setQuestion5(this.question5);
        surveyVO.setQuestion6(this.question6);
        surveyVO.setQuestion7(this.question7);
        surveyVO.setQuestion8(this.question8);
        surveyVO.setQuestion9(this.question9);
        surveyVO.setQuestion10(this.question10);
        return surveyVO;
    }
//    // SurveyVO를 SurveyDTO로 변환하는 정적 팩토리 메서드
//    public static SurveyDTO of(SurveyVO surveyVO) {
//
//        return SurveyDTO.builder()
//                .id(surveyVO.getId())
//                .totalScore(surveyVO.getTotalScore())
//                .propensityType(surveyVO.getPropensityType())
//                // VO의 개별 질문들을 DTO의 List<String>으로 묶음
//                .answers(Arrays.asList(
//                        surveyVO.getQuestion1(),
//                        surveyVO.getQuestion2(),
//                        surveyVO.getQuestion3(),
//                        surveyVO.getQuestion4(),
//                        surveyVO.getQuestion5()
//                ))
//                .build();
//    }
//
//    // SurveyDTO를 SurveyVO로 변환하는 메서드
//    public SurveyVO toVO() {
//
//        return SurveyVO.builder()
//                .id(this.id)
//                .totalScore(this.totalScore)
//                .propensityType(this.propensityType)
//                // DTO의 List<String> 질문들을 VO의 개별 필드에 매핑
//                .question1(this.answers.get(0))
//                .question2(this.answers.get(1))
//                .question3(this.answers.get(2))
//                .question4(this.answers.get(3))
//                .question5(this.answers.get(4))
//                .build();
//    }
}