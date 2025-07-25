package org.scoula.common.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final int status;

    public BaseException(String message, int status) {
        super(message);
        this.status = status;
    }

}