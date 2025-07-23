package org.scoula.transactions.exception;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(String id) {
        super("거래내역을 찾을 수 없습니다. ID = " + id);
    }
}
