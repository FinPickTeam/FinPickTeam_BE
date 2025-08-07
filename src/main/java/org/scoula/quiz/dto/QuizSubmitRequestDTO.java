package org.scoula.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.quiz.domain.QuizHistoryVO;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuizSubmitRequestDTO {

    private Long quizId;
    private Boolean isCorrect;

    public QuizHistoryVO toVO() {
        QuizHistoryVO quizHistoryVO = new QuizHistoryVO();
        quizHistoryVO.setQuizId(null);
        quizHistoryVO.setUserId(null);
        quizHistoryVO.setQuizId(quizId);
        quizHistoryVO.setIsCorrect(isCorrect);
        quizHistoryVO.setSubmittedAt(LocalDateTime.now());
        return quizHistoryVO;
    }
}
