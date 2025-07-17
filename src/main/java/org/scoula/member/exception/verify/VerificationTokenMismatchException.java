package org.scoula.member.exception.verify;

import org.scoula.member.exception.BaseException;

public class VerificationTokenMismatchException extends BaseException {
    public VerificationTokenMismatchException() {
        super("인증 토큰이 일치하지 않습니다.", 400);
    }
}
