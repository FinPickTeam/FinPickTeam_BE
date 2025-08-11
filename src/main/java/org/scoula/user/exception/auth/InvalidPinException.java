package org.scoula.user.exception.auth;

import org.scoula.common.exception.BaseException;

public class InvalidPinException extends BaseException {
    public InvalidPinException() {super("간편비밀번호가 올바르지 않습니다.", 401);}
}
