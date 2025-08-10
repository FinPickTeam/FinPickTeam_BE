package org.scoula.survey.service;

import lombok.RequiredArgsConstructor;

import lombok.extern.log4j.Log4j2;
import org.scoula.survey.domain.SurveyVO;
import org.scoula.survey.dto.SurveyDTO;
import org.scoula.survey.dto.SurveyRequestDTO;
import org.scoula.survey.dto.SurveyResponseDTO;
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
    public SurveyResponseDTO insert(Long userId, SurveyRequestDTO surveyRequestDTO) {

        log.info("설문 데이터 처리 시작: {}, {}",userId,surveyRequestDTO);

        // 1. 답변 점수 계산
        int totalScore = calculateTotalScore(surveyRequestDTO.getAnswers());

        // 2. 투자 성향 타입 결정
        String propensityType = determinePropensityType(totalScore);
        String propensityTypeExplain=null;

        switch (propensityType) {
            case "안정형": propensityTypeExplain="금융 지식이 부족하거나 원금 보존을 가장 중요하게 생각합니다. 투자 경험이 거의 없고 생활비, 교육비 등 단기 목적의 자금 마련을 목표로 하는 경향이 있습니다. 원금 손실을 극도로 싫어하는 가장 보수적인 유형입니다."; break;
            case "안정추구형": propensityTypeExplain="기본적인 금융 상품에 대한 지식은 있지만, 위험보다는 원금 보존을 더 중요하게 생각합니다. 주택 마련과 같은 중장기 목적의 자금을 운용하며, 비교적 낮은 위험의 상품(채권형 펀드 등)에 투자한 경험이 있을 수 있습니다."; break;
            case "위험중립형": propensityTypeExplain="널리 알려진 금융 상품에 대한 이해도가 높고, 원금 보존과 투자 수익을 모두 중요하게 생각합니다. 주식 등 원금 손실 위험이 있는 상품에 대한 경험도 있습니다. 적절한 위험을 감수하고 수익을 추구하는 균형 잡힌 유형입니다."; break;
            case "적극투자형": propensityTypeExplain="금융 지식이 깊고 투자 수익을 원금 보존보다 더 중요하게 여깁니다. 자산 증식을 위한 투자를 적극적으로 고려하며, 다양한 주식이나 비보장형 ELS 등 손실 위험이 있는 상품에 투자한 경험이 많습니다."; break;
            case "공격투자형": propensityTypeExplain="파생상품을 포함한 대부분의 금융 상품에 대한 구조와 위험을 완벽하게 이해하고 있습니다. 손실 위험을 감수하고서라도 높은 수익을 얻고자 하는 성향이 매우 강하며, 선물·옵션과 같은 고위험 파생상품에 투자한 경험이 풍부합니다. 가장 높은 수준의 위험을 감수하는 유형입니다."; break;
        }

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


        return SurveyResponseDTO.builder()
                .propensityType(propensityType)
                .propensityTypeExplain(propensityTypeExplain)
                .build();
    }



    //투자성향 가져오기
    @Override
    public SurveyDTO get(Long userId) {

        SurveyVO surveyVO=mapper.selectById(userId);

        return SurveyDTO.of(surveyVO);
    }


    // (6) 금융 지식 질문에 대한 점수
    private int getScoreForAnswer6(String answer) {
        switch (answer) {
            case "널리 알려진 금융투자상품(주식, 채권 및 펀드 등)의 구조 및 위험을 일정 부분 이해하고 있음": return 1;
            case "널리 알려진 금융투자상품(주식, 채권 및 펀드 등)의 구조 및 위험을 깊이 있게 이해하고 있음": return 3;
            case "파생상품을 포함한 대부분의 금융투자상품의 구조 및 위험을 이해하고 있음": return 5;
            default: throw new IllegalArgumentException("유효하지 않은 답변입니다: " + answer);
        }
    }

    // (7) 투자 목적 질문에 대한 점수
    private int getScoreForAnswer7(String answer) {
        switch (answer) {
            case "생활비": return 1;
            case "교육비": return 1;
            case "채무상환": return 1;
            case "결혼자금": return 1;
            case "자산중식": return 3;
            case "주택마련": return 2;
            default: throw new IllegalArgumentException("유효하지 않은 답변입니다: " + answer);
        }
    }

    // (8) 투자 수익 및 위험에 대한 태도 질문에 대한 점수
    private int getScoreForAnswer8(String answer) {
        switch (answer) {
            case "투자 수익을 고려하나 원금 보존이 더 중요": return 1;
            case "원금 보존을 고려하나 투자 수익이 더 중요": return 3;
            case "손실 위험이 있더라도 투자 수익이 더 중요": return 5;
            default: throw new IllegalArgumentException("유효하지 않은 답변입니다: " + answer);
        }
    }

    // (9) 투자 경험 질문에 대한 점수
    private int getScoreForAnswer9(String answer) {
        switch (answer) {
            case "경험없음": return 1;
            case "예금, CMA, MMF, RP, 국공채 등": return 1;
            case "채권형펀드, 원금보장형 ELS, 신용도가 A-이상인 채권 등": return 2;
            case "주식, 주식형펀드, 원금이 보장되지 않는 ELS, 신용도가 BBB- 이하인 채권 등": return 3;
            case "선물·옵션, 신용거래, ELW, 파생상품펀드 등": return 5;
            default: throw new IllegalArgumentException("유효하지 않은 답변입니다: " + answer);
        }
    }

    // 총점 계산 메서드
    private int calculateTotalScore(List<String> answers) {
        int score = 0;
        // answers 리스트의 순서가 질문의 순서와 일치한다고 가정합니다.
        score += getScoreForAnswer6(answers.get(0));
        score += getScoreForAnswer7(answers.get(1));
        score += getScoreForAnswer8(answers.get(2));
        score += getScoreForAnswer9(answers.get(3));
        return score;
    }

    // 투자 성향 타입 결정 메서드
    private String determinePropensityType(int totalScore) {
        if (totalScore >= 4 && totalScore <= 6) {
            return "안정형";
        } else if (totalScore >= 7 && totalScore <= 9) {
            return "안정추구형";
        } else if (totalScore >= 10 && totalScore <= 12) {
            return "위험중립형";
        } else if (totalScore >= 13 && totalScore <= 15) {
            return "적극투자형";
        } else if (totalScore >= 16 && totalScore <= 18) {
            return "공격투자형";
        } else {
            throw new IllegalArgumentException("유효하지 않은 총점입니다: " + totalScore);
        }

    }
}
