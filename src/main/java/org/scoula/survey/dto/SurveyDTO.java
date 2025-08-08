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
    private List<String> answers; // 질문 답변들을 List<String>으로 받음

    // SurveyVO를 SurveyDTO로 변환하는 정적 팩토리 메서드
    public static SurveyDTO of(SurveyVO surveyVO) {

        return SurveyDTO.builder()
                .id(surveyVO.getId())
                .totalScore(surveyVO.getTotalScore())
                .propensityType(surveyVO.getPropensityType())
                // VO의 개별 질문들을 DTO의 List<String>으로 묶음
                .answers(Arrays.asList(
                        surveyVO.getQuestion1(),
                        surveyVO.getQuestion2(),
                        surveyVO.getQuestion3(),
                        surveyVO.getQuestion4()
                ))
                .build();
    }

    // SurveyDTO를 SurveyVO로 변환하는 메서드
    public SurveyVO toVO() {

        return SurveyVO.builder()
                .id(this.id)
                .totalScore(this.totalScore)
                .propensityType(this.propensityType)
                // DTO의 List<String> 질문들을 VO의 개별 필드에 매핑
                .question1(this.answers.get(0))
                .question2(this.answers.get(1))
                .question3(this.answers.get(2))
                .question4(this.answers.get(3))
                .build();
    }
}