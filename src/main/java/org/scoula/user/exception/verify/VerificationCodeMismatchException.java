package org.scoula.user.exception.verify;

import org.scoula.common.exception.BaseException;

public class VerificationCodeMismatchException extends BaseException {
    public VerificationCodeMismatchException() {
        // 잘못된 입력 → 400
        super("인증 코드가 일치하지 않습니다.", 400);
    }
}
