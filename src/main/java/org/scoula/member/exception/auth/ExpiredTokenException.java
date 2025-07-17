package org.scoula.member.exception.auth;

import org.scoula.member.exception.BaseException;

public class ExpiredTokenException extends BaseException {
    public ExpiredTokenException() {
        super("토큰이 만료되었습니다. 다시 로그인해주세요.", 401);
    }
}
