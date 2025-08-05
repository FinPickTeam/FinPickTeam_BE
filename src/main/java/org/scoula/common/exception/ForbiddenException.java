package org.scoula.common.exception;

import org.scoula.common.exception.BaseException;

public class ForbiddenException extends BaseException {
    public ForbiddenException(String message) {
        super(message, 403); // 👈 여기 status 403 같이 넘겨야 함
    }
}