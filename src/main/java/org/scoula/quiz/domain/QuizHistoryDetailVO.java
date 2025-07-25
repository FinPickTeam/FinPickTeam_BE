package org.scoula.quiz.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizHistoryDetailVO {
    Long historyId;
    String question;
    String answer;
    Boolean isCorrect;
}
