package org.scoula.transactions.exception;

import org.scoula.common.exception.BaseException;

public class AccountTransactionNotFoundException extends BaseException {
    public AccountTransactionNotFoundException() {
        super(AccountTransactionErrorCode.ACCOUNT_TX_NOT_FOUND.getMessage(), AccountTransactionErrorCode.ACCOUNT_TX_NOT_FOUND.getStatus());
    }
}
