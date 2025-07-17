package org.scoula.member.exception.verify;

import org.scoula.member.exception.BaseException;

public class VerificationTokenNotFoundException extends BaseException {
    public VerificationTokenNotFoundException() {
        super("인증 토큰이 존재하지 않습니다.", 404);
    }
}
