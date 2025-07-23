package org.scoula.user.exception.signup;

import org.scoula.common.exception.BaseException;

public class PasswordMismatchException extends BaseException {
    public PasswordMismatchException() {
        super("비밀번호가 일치하지 않습니다.", 400);
    }
}
