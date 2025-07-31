package org.scoula.transactions.exception;

import org.scoula.common.exception.BaseException;

public class LedgerNotFoundException extends BaseException {
    public LedgerNotFoundException() {
        super(LedgerErrorCode.LEDGER_NOT_FOUND.getMessage(), LedgerErrorCode.LEDGER_NOT_FOUND.getStatus());
    }
}
