package org.scoula.transactions.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LedgerErrorCode {
    LEDGER_NOT_FOUND(404, "거래 내역이 존재하지 않습니다."),
    LEDGER_ACCESS_DENIED(403, "해당 거래에 접근할 수 없습니다.");

    private final int status;
    private final String message;
}
