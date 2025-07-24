package org.scoula.survey.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.survey.dto.SurveyDTO;
import org.scoula.survey.service.SurveyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    // 일반적으로 Form Data 또는 URL Encoded Form (x-www-form-urlencoded)으로 전송될 때 사용
    @ApiOperation(value="투자성향저장", notes = "투자성향을 저장한다")
    @PostMapping("/submit-params")
    public ResponseEntity<String> submitSurvey(
            @RequestParam("userId") Long userId, // userId 파라미터
            @RequestParam("answers[]") List<String> answers // answers[] 파라미터 (배열로 받음)
    ) {
        log.info("설문 제출 요청 수신 (파라미터): userId={}, answers={}", userId, answers);

        //입력 유효성 검사
        if (userId == null || answers == null || answers.size() != 4) { // 답변 개수 4개로 가정
            log.warn("필수 설문 데이터 누락 또는 형식 오류: userId={}, answers={}", userId, answers);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            SurveyDTO surveyDTO = SurveyDTO.builder()
                    .id(userId)
                    .question1(answers.get(0))
                    .question2(answers.get(1))
                    .question3(answers.get(2))
                    .question4(answers.get(3))
                    .build();

            // 3. 서비스 계층으로 설문 데이터 전달 및 처리 요청
            SurveyDTO resultDTO = surveyService.insert(surveyDTO);
            log.info("설문 처리 및 저장 성공: {}", resultDTO);

            // 4. 성공 응답 반환 (201 Created)
            return new ResponseEntity<>(resultDTO.getPropensityType(), HttpStatus.CREATED);

        } catch (IndexOutOfBoundsException e) {
            log.error("answers 배열의 길이가 예상과 다릅니다: {}", e.getMessage());
            return new ResponseEntity<>("answers 배열의 길이가 예상과 다릅니다",HttpStatus.BAD_REQUEST); // 400 Bad Request 반환
        } catch (IllegalArgumentException e) {
            log.error("설문 처리 중 잘못된 인자 오류 (예: 유효하지 않은 답변): {}", e.getMessage());
            return new ResponseEntity<>("설문 처리 중 잘못된 인자 오류",HttpStatus.BAD_REQUEST); // 400 Bad Request 반환
        } catch (Exception e) {
            log.error("설문 처리 중 서버 오류 발생: {}", e.getMessage(), e);
            return new ResponseEntity<>("설문 처리 중 서버 오류 발생",HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error 반환
        }
    }

    @ApiOperation(value = "투자성향조회", notes = "투자성향을 조회한다")
    @GetMapping("/userId={userId}")
    public ResponseEntity<String> get(@PathVariable("userId") Long userId) {
        log.info("ID{}로 투자 성향 타입 조회 요청 수신", userId);

        try {
            SurveyDTO surveyDTO = surveyService.get(userId);

            if (surveyDTO != null) {
                log.info("ID {}의 투자 성향 타입 조회 성공: {}", userId, surveyDTO.getPropensityType());

                return new ResponseEntity<>(surveyDTO.getPropensityType(), HttpStatus.OK); // 200 OK와 함께 타입 반환
            } else {
                log.warn("ID {}에 해당하는 투자 성향 타입을 찾을 수 없습니다.", userId);
                return new ResponseEntity<>("ID에 해당하는 투자 성향 타입을 찾을 수 없습니다.", HttpStatus.NOT_FOUND); // 404 Not Found
            }
        } catch (Exception e) {
            log.error("투자 성향 타입 조회 중 서버 오류 발생: {}", e.getMessage(), e);
            return new ResponseEntity<>("투자 성향 타입 조회 중 서버 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
        }


    }


}