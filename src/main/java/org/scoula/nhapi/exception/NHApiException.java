package org.scoula.nhapi.exception;

import org.scoula.common.exception.BaseException;

public class NHApiException extends BaseException {
    public NHApiException(String message) {
        super(message, 400); // or 502 depending on NH API error
    }

    public NHApiException(String message, Throwable cause) {
        super(message, 500);
        initCause(cause);
    }
}
