package org.scoula.challenge.exception.join;

import org.scoula.common.exception.BaseException;

public class ChallengePasswordMismatchException extends BaseException {
    public ChallengePasswordMismatchException() {
        super("비밀번호가 일치하지 않습니다.", 403);
    }
}
