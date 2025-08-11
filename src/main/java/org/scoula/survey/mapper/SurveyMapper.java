package org.scoula.survey.mapper;

import org.scoula.survey.domain.SurveyVO;
import org.scoula.survey.dto.SurveyRequestDTO;

public interface SurveyMapper {

    //온보딩 시 투자성향결과 삽입
    void insert(SurveyVO surveyVO);

    //투자성향 수정
    void update(SurveyVO surveyVO);

    //전체설문완료여부 체크
    Boolean isSurvey(Long userId);

    //투자성향 검색
    SurveyVO selectById(Long id);
}
