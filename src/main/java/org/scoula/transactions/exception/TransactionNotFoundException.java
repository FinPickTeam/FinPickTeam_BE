package org.scoula.transactions.exception;

import org.scoula.common.exception.BaseException;

public class TransactionNotFoundException extends BaseException {
    public TransactionNotFoundException(Long id) {
        super("거래내역을 찾을 수 없습니다. ID = " + id, 404);
    }
}

