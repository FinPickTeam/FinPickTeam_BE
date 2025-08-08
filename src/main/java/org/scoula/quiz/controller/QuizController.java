package org.scoula.quiz.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.quiz.domain.QuizHistoryDetailVO;
import org.scoula.quiz.dto.QuizDTO;
import org.scoula.quiz.dto.QuizHistoryDTO;
import org.scoula.quiz.dto.QuizHistoryDetailDTO;
import org.scoula.quiz.dto.QuizSubmitRequestDTO;
import org.scoula.quiz.service.QuizService;
import org.scoula.security.account.domain.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = {"퀴즈API"}, description="Quiz Controller")
@RequestMapping("/api/Quiz")
public class QuizController {

    final private QuizService quizService;


    @ApiOperation(value = "오늘의퀴즈 조회", notes = "오늘의 퀴즈를 랜덤으로 제공합니다. 유저가 이전에 풀었던 문제는 필터링합니다.")
    @GetMapping("/todaysQuiz")
    public ResponseEntity<CommonResponseDTO<QuizDTO>> getQuiz(@AuthenticationPrincipal CustomUserDetails userDetails) {
        QuizDTO todaysQuiz = quizService.getQuiz(userDetails.getUserId());
        return ResponseEntity.ok(CommonResponseDTO.success("퀴즈 조회 성공", todaysQuiz));
    }

    @ApiOperation(value = "퀴즈응시기록 저장", notes = "퀴즈응시기록을 저장합니다.")
    @PostMapping("/submit")
    public ResponseEntity<CommonResponseDTO<QuizDTO>> submit(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody QuizSubmitRequestDTO quizSubmitRequestDTO) {
        quizService.submit(userDetails.getUserId(),quizSubmitRequestDTO);
        return ResponseEntity.ok(CommonResponseDTO.success("저장성공"));
    }

    @ApiOperation(value = "퀴즈히스토리 내역조회", notes = "유저의 퀴즈응시기록들을 조회합니다.")
    @GetMapping("/HistoryList")
    public ResponseEntity<CommonResponseDTO<List<QuizHistoryDetailDTO>>> HistoryList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<QuizHistoryDetailDTO> historyList = quizService.getHistoryList(userDetails.getUserId());
        return ResponseEntity.ok(CommonResponseDTO.success("퀴즈히스토리 내역조회 성공",historyList));
    }

    @ApiOperation(value = "퀴즈히스토리 상세조회", notes = "퀴즈응시기록들을 상세조회합니다.")
    @GetMapping("/HistoryDetail={quizId}")
    public ResponseEntity<CommonResponseDTO<QuizHistoryDetailDTO>> HistoryDetail(@PathVariable Long quizId) {
        QuizHistoryDetailDTO QuizhistoryDetail = quizService.getHistoryDetail(quizId);
        return ResponseEntity.ok(CommonResponseDTO.success("퀴즈히스토리 상세조회성공",QuizhistoryDetail));
    }
}
