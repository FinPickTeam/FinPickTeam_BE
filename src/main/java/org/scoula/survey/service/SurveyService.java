package org.scoula.survey.service;

import org.scoula.survey.dto.SurveyDTO;

public interface SurveyService {

    //유저 투자성향 저장
    SurveyDTO insert(SurveyDTO surveyDTO);

    //투자성향 불러오기
    SurveyDTO get(Long Id);

    //SurveyDTO update(SurveyDTO surveyDTO);

}