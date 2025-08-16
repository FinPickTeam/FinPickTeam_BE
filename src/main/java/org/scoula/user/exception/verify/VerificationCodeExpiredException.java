package org.scoula.user.exception.verify;

import org.scoula.common.exception.BaseException;

public class VerificationCodeExpiredException extends BaseException {
    public VerificationCodeExpiredException() {
        // 410 Gone 또는 400 중 택1. 만료는 410을 자주 씀.
        super("인증 코드가 만료되었습니다. 재전송 해주세요.", 410);
    }
}
