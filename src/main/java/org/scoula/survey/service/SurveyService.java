package org.scoula.survey.service;

import org.scoula.survey.dto.SurveyDTO;
import org.scoula.survey.dto.SurveyRequestDTO;
import org.scoula.survey.dto.SurveyResponseDTO;

public interface SurveyService {

    //유저 투자성향 저장
    SurveyResponseDTO insert(Long userId, SurveyRequestDTO surveyRequestDTO);

    //투자성향 불러오기
    SurveyDTO get(Long Id);

    //SurveyDTO update(SurveyDTO surveyDTO);

}