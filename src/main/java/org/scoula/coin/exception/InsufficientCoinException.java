package org.scoula.coin.exception;

import org.scoula.common.exception.BaseException;

public class InsufficientCoinException extends BaseException {
    public InsufficientCoinException() {
        super("보유한 포인트가 부족합니다. (최소 100P 필요)", 400);
    }
}
