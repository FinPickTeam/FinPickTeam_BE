package org.scoula.survey.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.security.account.domain.CustomUserDetails;
import org.scoula.survey.dto.SurveyDTO;
import org.scoula.survey.dto.SurveyRequestDTO;
import org.scoula.survey.dto.SurveyResponseDTO;
import org.scoula.survey.service.SurveyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List; // List<String>을 사용하려면 임포트


@Slf4j
@RequiredArgsConstructor
@Api(tags = {"투자성향API"}, description="Survey Cotroller")
@RestController
@RequestMapping("/api/survey")
public class SurveyController {

    private final SurveyService surveyService;


    // POST 요청으로 userId와 answers[]를 받는 경우
    @ApiOperation(value="투자성향저장", notes = "투자성향을 저장한다")
    @PostMapping("/submit-params")
    public ResponseEntity<CommonResponseDTO<SurveyResponseDTO>> submitSurvey(@AuthenticationPrincipal CustomUserDetails userDetails, @ModelAttribute SurveyRequestDTO surveyRequestDTO) {
        log.info("설문 제출 요청 수신 (파라미터): userId={}, answers={}", userDetails.getUserId(), surveyRequestDTO.getAnswers());
        SurveyResponseDTO resultDTO = surveyService.insert(userDetails.getUserId(),surveyRequestDTO);
        log.info("설문 처리 및 저장 성공: {}", resultDTO);

        return ResponseEntity.ok(CommonResponseDTO.success("설문저장성공",resultDTO));
    }

    @ApiOperation(value = "투자성향조회", notes = "투자성향을 조회한다")
    @GetMapping("/userId={userId}")
    public  ResponseEntity<CommonResponseDTO<SurveyDTO>> getSurvey(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("ID{}로 투자 성향 타입 조회 요청 수신", userDetails.getUserId());
        SurveyDTO surveyDTO = surveyService.get(userDetails.getUserId());
        log.info("ID {}의 투자 성향 타입 조회 성공: {}", userDetails.getUserId(), surveyDTO.getPropensityType());
        return ResponseEntity.ok(CommonResponseDTO.success("투자성향설문 기록 조회 성공",surveyDTO));
    }
}