package org.scoula.member.exception.auth;

import org.scoula.member.exception.BaseException;

public class InvalidTokenException extends BaseException {
    public InvalidTokenException() {
        super("유효하지 않은 토큰입니다.", 401);
    }
}
