package org.scoula.user.exception.signup;

import org.scoula.common.exception.BaseException;

public class DuplicateEmailException extends BaseException {
    public DuplicateEmailException() {
        super("이미 사용 중인 이메일입니다.", 409);
    }
}
