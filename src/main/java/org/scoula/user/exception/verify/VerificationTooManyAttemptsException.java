package org.scoula.user.exception.verify;

import org.scoula.common.exception.BaseException;

public class VerificationTooManyAttemptsException extends BaseException {
    public VerificationTooManyAttemptsException() {
        // 시도 과다 → 429
        super("인증 시도가 너무 많습니다. 잠시 후 다시 시도해주세요.", 429);
    }
}
