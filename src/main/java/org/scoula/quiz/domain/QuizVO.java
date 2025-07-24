package org.scoula.quiz.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizVO {
    private Long id;
    private String question;
    private char answer;
    private String explanation;
}
