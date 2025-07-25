package org.scoula.quiz.service;

import org.scoula.quiz.dto.QuizDTO;
import org.scoula.quiz.dto.QuizHistoryDTO;
import org.scoula.quiz.dto.QuizHistoryDetailDTO;

import java.util.List;


public interface QuizService {

     QuizDTO getQuiz(Long userId);

     void submit(QuizHistoryDTO quizHistoryDTO);

     QuizHistoryDetailDTO getHistoryDetail(Long historyId);

     List<QuizHistoryDetailDTO> getHistoryList(Long userId);
}
