package org.scoula.nhapi.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class NhCardTransactionResponseDto {
    private String authNumber;
    private String salesType;
    private String approvedAt;
    private String paymentDate;
    private BigDecimal amount;
    private boolean isCancelled;
    private BigDecimal cancelAmount;
    private String cancelledAt;
    private String merchantName;
    private String tpbcd;
    private String tpbcdNm;
    private int installmentMonth;
    private String currency;
    private BigDecimal foreignAmount;
}
