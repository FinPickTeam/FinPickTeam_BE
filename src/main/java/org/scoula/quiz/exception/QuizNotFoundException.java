package org.scoula.quiz.exception;

import org.scoula.common.exception.BaseException;

public class QuizNotFoundException extends BaseException {
    public QuizNotFoundException(Long id) {
        super("응시가능한 퀴즈가 없습니다. ID = " + id, 500);
    }
}

