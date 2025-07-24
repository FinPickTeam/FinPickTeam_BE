package org.scoula.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder; // Lombok Builder 어노테이션 추가
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.quiz.domain.QuizVO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizDTO {
    private Long id;
    private String question;
    private char answer;
    private String explanation;


    public static QuizDTO of(QuizVO quizVO) {
        if (quizVO == null) {
            return null;
        }
        return QuizDTO.builder()
                .id(quizVO.getId())
                .question(quizVO.getQuestion())
                .answer(quizVO.getAnswer())
                .explanation(quizVO.getExplanation())
                .build();
    }


    public QuizVO toVO() {
        return new QuizVO(this.id, this.question, this.answer, this.explanation);
    }
}
