package org.scoula.quiz.service;

import lombok.RequiredArgsConstructor;
import org.scoula.quiz.domain.QuizHistoryVO;
import org.scoula.quiz.domain.QuizVO;
import org.scoula.quiz.dto.QuizDTO;
import org.scoula.quiz.exception.QuizAlreadyTakenTodayException;
import org.scoula.quiz.exception.QuizNotFoundException;
import org.scoula.quiz.mapper.QuizMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    final private QuizMapper quizMapper;

    @Override
    public QuizDTO getQuiz(Long userId) {

        //금일 퀴즈응시한 데이터가 있는지 검사
        if(quizMapper.isQuizTakenToday(userId)>0){
            throw new QuizAlreadyTakenTodayException(userId);
        }
        QuizVO quizVO = quizMapper.getQuiz(userId);

        QuizDTO quizDTO = QuizDTO.of(quizVO);

        //응시가능한 데이터가 없을 경우, 예외처리
        if(quizDTO == null){
            throw new QuizNotFoundException(userId);
        }

        return quizDTO;
    }

    @Override
    public void submit(Long userId, Long quizId, boolean isCorrect) {
        QuizHistoryVO quizHistoryVO = new QuizHistoryVO();

        quizHistoryVO.setUserId(userId);
        quizHistoryVO.setQuizId(quizId);
        quizHistoryVO.setIsCorrect(isCorrect);
        quizHistoryVO.setSubmittedAt(LocalDateTime.now());
    }


}
