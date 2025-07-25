package org.scoula.transactions.exception;

import org.scoula.common.exception.BaseException;

public class AccountNotFoundException extends BaseException {
    public AccountNotFoundException(Long accountId) {
        super("해당 계좌가 존재하지 않습니다. ID = " + accountId, 404);
    }
}
