package org.scoula.ibkapi.exception;

import lombok.Getter;
import org.scoula.common.exception.BaseException;

@Getter
public class CardException extends BaseException {
    private final CardErrorCode errorCode;

    public CardException(CardErrorCode errorCode) {
        super(errorCode.getMessage(), errorCode.getStatus());
        this.errorCode = errorCode;
    }
}
