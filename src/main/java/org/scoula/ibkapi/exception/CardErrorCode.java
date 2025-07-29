package org.scoula.ibkapi.exception;

import lombok.Getter;

@Getter
public enum CardErrorCode {
    CARD_NOT_FOUND(404, "카드 정보를 찾을 수 없습니다."),
    DUPLICATE_CARD(409, "이미 등록된 카드입니다."),
    TRANSACTION_NOT_FOUND(404, "승인내역을 찾을 수 없습니다."),
    DUPLICATE_TRANSACTION(409, "이미 등록된 승인내역입니다."),
    API_CALL_FAILED(502, "IBK 카드 API 호출에 실패했습니다."),
    INVALID_RESPONSE(500, "카드 API 응답 형식이 올바르지 않습니다.");

    private final int status;
    private final String message;

    CardErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
