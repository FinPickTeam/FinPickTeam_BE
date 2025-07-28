package org.scoula.quiz.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.quiz.domain.QuizHistoryDetailVO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizHistoryDetailDTO {
    Long historyId;
    String question;
    String answer;
    Boolean isCorrect;

    public static QuizHistoryDetailDTO of(QuizHistoryDetailVO quizHistoryDetailVO) {

        return QuizHistoryDetailDTO .builder()
                .historyId(quizHistoryDetailVO.getHistoryId())
                .question(quizHistoryDetailVO.getQuestion())
                .answer(quizHistoryDetailVO.getAnswer())
                .isCorrect(quizHistoryDetailVO.getIsCorrect())
                .build();
    }


    public QuizHistoryDetailVO toVO(){
        QuizHistoryDetailVO quizHistoryDetailVO = new QuizHistoryDetailVO();
        quizHistoryDetailVO.setHistoryId(historyId);
        quizHistoryDetailVO.setQuestion(question);
        quizHistoryDetailVO.setAnswer(answer);
        quizHistoryDetailVO.setIsCorrect(isCorrect);
        return quizHistoryDetailVO;
    }
}