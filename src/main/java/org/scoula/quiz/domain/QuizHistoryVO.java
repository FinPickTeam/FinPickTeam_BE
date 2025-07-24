package org.scoula.quiz.domain;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizHistoryVO {
    private Long id;
    private Long userId;
    private Long quizId;
    private Boolean isCorrect;
    private LocalDateTime submittedAt;
}
