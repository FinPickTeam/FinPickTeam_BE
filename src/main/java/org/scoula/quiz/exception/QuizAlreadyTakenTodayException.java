package org.scoula.quiz.exception;

import org.scoula.common.exception.BaseException;

// 사용자가 오늘 이미 퀴즈를 풀었을 때 발생하는 예외
public class QuizAlreadyTakenTodayException extends BaseException {
    public QuizAlreadyTakenTodayException(Long userId) {
        super("사용자 ID " + userId + "님은 오늘 이미 퀴즈를 풀었습니다. 내일 다시 시도해주세요.", 409); // 409 Conflict 상태 코드 사용
    }
}
