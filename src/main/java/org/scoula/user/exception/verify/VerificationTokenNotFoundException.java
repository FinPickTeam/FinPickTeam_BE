package org.scoula.user.exception.verify;

import org.scoula.common.exception.BaseException;

public class VerificationTokenNotFoundException extends BaseException {
    public VerificationTokenNotFoundException() {
        super("인증 토큰이 존재하지 않습니다.", 404);
    }
}
