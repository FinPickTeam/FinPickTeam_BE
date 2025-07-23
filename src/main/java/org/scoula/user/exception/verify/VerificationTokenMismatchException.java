package org.scoula.user.exception.verify;

import org.scoula.common.exception.BaseException;

public class VerificationTokenMismatchException extends BaseException {
    public VerificationTokenMismatchException() {
        super("인증 토큰이 일치하지 않습니다.", 400);
    }
}
