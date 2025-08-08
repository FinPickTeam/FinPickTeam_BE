package org.scoula.challenge.exception;

import org.scoula.common.exception.BaseException;

public class ChallengePasswordFormatException extends BaseException {
    public ChallengePasswordFormatException() {
        super("비밀번호는 숫자 4자리여야 합니다.", 400);
    }
}
