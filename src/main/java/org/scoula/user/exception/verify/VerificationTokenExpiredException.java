package org.scoula.user.exception.verify;

import org.scoula.common.exception.BaseException;

public class VerificationTokenExpiredException extends BaseException {
    public VerificationTokenExpiredException() {
        super("인증 토큰이 만료되었습니다.", 400);
    }
}
