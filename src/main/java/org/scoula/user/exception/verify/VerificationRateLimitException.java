package org.scoula.user.exception.verify;

import org.scoula.common.exception.BaseException;

public class VerificationRateLimitException extends BaseException {
    public VerificationRateLimitException() {
        // 전송 제한/쿨타임 → 429
        super("인증 메일 전송 제한에 도달했습니다. 잠시 후 다시 시도해주세요.", 429);
    }
}
