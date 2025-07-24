package org.scoula.survey.mapper;

import org.scoula.survey.domain.SurveyVO;

public interface SurveyMapper {
    //온보딩 시 투자성향결과 삽입
    void insertSurvey(SurveyVO surveyVO);
    //투자성향 검색
    SurveyVO selectById(Long id);
    //투자성향 바꾸기-만약 투자성향 수정가능하다면 개발
    //void updateSurveyResult(InvestmentTypes propensity);
}
