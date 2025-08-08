package org.scoula.challenge.exception;

import org.scoula.common.exception.BaseException;

public class StartDateAfterEndDateException extends BaseException {
    public StartDateAfterEndDateException() {
        super("시작일은 종료일보다 이후일 수 없습니다.", 400);
    }
}
