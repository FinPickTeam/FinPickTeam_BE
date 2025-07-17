package org.scoula.member.exception.auth;

import org.scoula.member.exception.BaseException;

public class InvalidEmailException extends BaseException {
    public InvalidEmailException() {
        super("존재하지 않는 이메일입니다.", 404);
    }
}