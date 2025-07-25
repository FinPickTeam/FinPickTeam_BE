package org.scoula.quiz.mapper;

import org.scoula.quiz.domain.QuizHistoryDetailVO;
import org.scoula.quiz.domain.QuizHistoryVO;
import org.scoula.quiz.domain.QuizVO;

import java.util.List;

public interface QuizMapper {

    QuizVO getQuiz(Long userId);
    int isQuizTakenToday(long userId);
    void insertHistory(QuizHistoryVO quizHistoryVO);
    List<QuizHistoryDetailVO> getHistoryList(Long userId);
    QuizHistoryDetailVO getHistoryDetail(Long historyId);
}