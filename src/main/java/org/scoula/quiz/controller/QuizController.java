package org.scoula.quiz.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.quiz.dto.QuizDTO;
import org.scoula.quiz.dto.QuizHistoryDTO;
import org.scoula.quiz.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/Quiz")
public class QuizController {

    final private QuizService quizService;


    @ApiOperation(value = "오늘의퀴즈 조회", notes = "오늘의 퀴즈를 랜덤으로 제공합니다. 유저가 이전에 풀었던 문제는 필터링합니다.")
    @GetMapping("/todaysQuiz")
    public ResponseEntity<CommonResponseDTO<QuizDTO>> getQuiz(@RequestParam Long userId) {
        QuizDTO todaysQuiz = quizService.getQuiz(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("퀴즈 조회 성공", todaysQuiz));
    }

    @ApiOperation(value = "퀴즈응시기록 저장", notes = "퀴즈응시기록을 저장합니다.")
    @PostMapping("/submit ")
    public ResponseEntity<CommonResponseDTO<QuizDTO>> submit(@RequestParam Long userId, @RequestParam Long quizId, @RequestParam boolean answer) {
        quizService.submit(userId,quizId,answer);
        return ResponseEntity.ok(CommonResponseDTO.success("저장성공"));
    }
}
