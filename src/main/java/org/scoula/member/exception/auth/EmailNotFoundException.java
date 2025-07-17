package org.scoula.member.exception.auth;

import org.scoula.member.exception.BaseException;

public class EmailNotFoundException extends BaseException {
    public EmailNotFoundException() {
        super("등록되지 않은 이메일입니다.", 404);
    }
}
