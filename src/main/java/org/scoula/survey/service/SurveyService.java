package org.scoula.survey.service;

import org.scoula.survey.dto.FirstSurveyRequestDTO;
import org.scoula.survey.dto.SurveyDTO;
import org.scoula.survey.dto.SurveyRequestDTO;
import org.scoula.survey.dto.SurveyResponseDTO;

public interface SurveyService {

    //온보딩 시 투자성향설문저장
    SurveyResponseDTO insert(Long userId, FirstSurveyRequestDTO firstsurveyRequestDTO);

    //추가설문 저쟝
    SurveyResponseDTO update(Long userId, SurveyRequestDTO surveyRequestDTO);

    //추가설문 했는지 여부 반환
    Boolean isSurveyCompleted (Long userId);

    //투자성향 불러오기
    SurveyDTO get(Long Id);


    //SurveyDTO update(SurveyDTO surveyDTO);

}