package org.scoula.user.exception.verify;

import org.scoula.common.exception.BaseException;

public class EmailAlreadyVerifiedException extends BaseException {
    public EmailAlreadyVerifiedException() {
        super("이미 인증된 이메일입니다.", 409);
    }
}
