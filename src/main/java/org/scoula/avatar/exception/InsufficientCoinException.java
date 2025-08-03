package org.scoula.avatar.exception;

import org.scoula.common.exception.BaseException;

public class InsufficientCoinException  extends BaseException {
    public InsufficientCoinException() {
        super("재화가 부족합니다", 409); // 409 Conflict 상태 코드 사용
    }
}
