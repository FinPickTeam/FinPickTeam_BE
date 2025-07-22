package org.scoula.user.exception.auth;

import org.scoula.common.exception.BaseException;

public class InvalidPasswordException extends BaseException {
    public InvalidPasswordException() {
        super("비밀번호가 올바르지 않습니다.", 401); // Unauthorized
    }
}