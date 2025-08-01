package org.scoula.transactions.exception;

import org.scoula.common.exception.BaseException;

public class CardTransactionNotFoundException extends BaseException {
    public CardTransactionNotFoundException() {
        super(CardTransactionErrorCode.CARD_TX_NOT_FOUND.getMessage(),CardTransactionErrorCode.CARD_TX_NOT_FOUND.getStatus());
    }
}
