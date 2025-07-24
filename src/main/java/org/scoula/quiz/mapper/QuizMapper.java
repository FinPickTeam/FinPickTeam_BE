package org.scoula.quiz.mapper;

import org.scoula.quiz.domain.QuizHistoryVO;
import org.scoula.quiz.domain.QuizVO;

public interface QuizMapper {

    QuizVO getQuiz(Long userId);
    int isQuizTakenToday(long userId);
    QuizHistoryVO insertHistory(Long userId,long quizId,boolean isCorrect);
}