package org.scoula.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.quiz.domain.QuizHistoryVO;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizHistoryDTO {
    private Long id;
    private Long userId;
    private Long quizId;
    private Boolean isCorrect;
    private LocalDateTime submittedAt;


    public static QuizHistoryDTO of(QuizHistoryVO quizHistoryVO) {

        return QuizHistoryDTO.builder()
                .id(quizHistoryVO.getId())
                .userId(quizHistoryVO.getUserId())
                .quizId(quizHistoryVO.getQuizId())
                .isCorrect(quizHistoryVO.getIsCorrect())
                .submittedAt(quizHistoryVO.getSubmittedAt())
                .build();
    }


    public QuizHistoryVO toVO() {

        return new QuizHistoryVO(this.id, this.userId, this.quizId, this.isCorrect, this.submittedAt);
    }
}