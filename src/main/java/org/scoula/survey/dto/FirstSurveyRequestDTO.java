package org.scoula.survey.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.survey.domain.SurveyVO; // SurveyVO 임포트

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data // Getter, Setter, equals, hashCode, toString 자동 생성
@NoArgsConstructor // 기본 생성자 자동 생성
@AllArgsConstructor // 모든 필드를 포함하는 생성자 자동 생성
@Builder // 빌더 패턴 사용
public class FirstSurveyRequestDTO {

    private String question1;
    private String question2;
    private String question3;
    private String question4;
    private String question5;
}