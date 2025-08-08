package org.scoula.survey.service;

import lombok.RequiredArgsConstructor;

import lombok.extern.log4j.Log4j2;
import org.scoula.survey.domain.SurveyVO;
import org.scoula.survey.dto.SurveyDTO;
import org.scoula.survey.dto.SurveyRequestDTO;
import org.scoula.survey.mapper.SurveyMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@Service
public class SurveyServiceImpl implements SurveyService {

    final SurveyMapper mapper;

    //투자성향저장
    @Override
    public SurveyDTO insert(Long userId, SurveyRequestDTO surveyRequestDTO) {

        log.info("설문 데이터 처리 시작: {}, {}",userId,surveyRequestDTO);


//        // 1. 답변 점수 계산
//        int totalScore = calculateTotalScore(
//                surveyRequestDTO.getAnswers()
//        );
//        surveyDTO.setTotalScore(totalScore); // 계산된 총점 DTO에 설정
//        log.info("총점 계산 완료: {}", totalScore);
//
//        // 2. 투자 성향 타입 결정
//        String propensityType = determinePropensityType(totalScore);
//        surveyDTO.setPropensityType(propensityType); // 결정된 투자 성향 타입 DTO에 설정
//        log.info("투자 성향 타입 결정 완료: {}", propensityType);

        // 1. 답변 점수 계산
        int totalScore = calculateTotalScore(surveyRequestDTO.getAnswers());
        // 2. 투자 성향 타입 결정
        String propensityType = determinePropensityType(totalScore);

        // 빌더 패턴을 사용하여 surveyDTO 객체를 한 번에 생성
        SurveyDTO surveyDTO = SurveyDTO.builder()
                .id(userId)
                .answers(surveyRequestDTO.getAnswers())
                .totalScore(totalScore)
                .propensityType(propensityType)
                .build();

        // 3. DTO를 VO로 변환하여 데이터베이스에 삽입
        SurveyVO surveyVO = surveyDTO.toVO();
        mapper.insertSurvey(surveyVO);

        log.info("데이터베이스 삽입 완료: {}", surveyVO);


        return get(surveyVO.getId());
    }

    //투자성향 가져오기
    @Override
    public SurveyDTO get(Long userId) {

        SurveyVO surveyVO=mapper.selectById(userId);

        return SurveyDTO.of(surveyVO);
    }


    private int calculateTotalScore(List<String> answers) {
        int score = 0;
        for (String answer : answers) {
            score += getScoreForAnswer(answer);
        }
        return score;
    }

    /**
     * 단일 답변에 대한 점수를 반환합니다.
     * @param answer 답변 문자열 (A, B, C)
     * @return 해당 답변의 점수
     * @throws IllegalArgumentException 유효하지 않은 답변인 경우
     */
    private int getScoreForAnswer(String answer) {
        switch (answer) {
            case "A": return 0;
            case "B": return 1;
            case "C": return 2;
            default: throw new IllegalArgumentException("유효하지 않은 답변입니다: " + answer);
        }
    }

    /**
     * 총점을 바탕으로 투자 성향 타입을 결정합니다.
     * 안정형 (0~2점), 유연형 (3~5점), 고위험 (6~8점)
     * @param totalScore 계산된 총점
     * @return 투자 성향 타입 문자열
     * @throws IllegalArgumentException 유효하지 않은 총점인 경우
     */
    private String determinePropensityType(int totalScore) {
        if (totalScore >= 0 && totalScore <= 2) {
            return "안정형";
        } else if (totalScore >= 3 && totalScore <= 5) {
            return "유연형";
        } else if (totalScore >= 6 && totalScore <= 8) {
            return "고위험";
        } else {
            // 이 경우는 일반적으로 발생하지 않아야 하지만, 안전을 위해 추가
            throw new IllegalArgumentException("유효하지 않은 총점입니다: " + totalScore);
        }
    }
}
