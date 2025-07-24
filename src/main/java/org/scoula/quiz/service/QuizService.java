package org.scoula.quiz.service;

import org.scoula.quiz.dto.QuizDTO;


public interface QuizService {

     QuizDTO getQuiz(Long userId);

     void submit(Long userId, Long quizId,boolean isCorrect);
}
