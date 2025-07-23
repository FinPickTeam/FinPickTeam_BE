package org.scoula.user.exception.signup;

import org.scoula.common.exception.BaseException;

public class InvalidPasswordFormatException extends BaseException {
    public InvalidPasswordFormatException() {
        super("비밀번호는 8자 이상이며, 영문/숫자/특수문자를 포함해야 합니다.", 400);
    }
}
