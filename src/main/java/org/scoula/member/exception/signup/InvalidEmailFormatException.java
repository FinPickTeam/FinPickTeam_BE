package org.scoula.member.exception.signup;

import org.scoula.member.exception.BaseException;

public class InvalidEmailFormatException extends BaseException {
    public InvalidEmailFormatException() {
        super("올바른 이메일 형식이 아닙니다.", 400);
    }
}
