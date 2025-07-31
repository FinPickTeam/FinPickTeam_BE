package org.scoula.transactions.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CardTransaction {
    private Long id;
    private Long userId;
    private Long cardId;
    private String authNumber;
    private String salesType;
    private LocalDateTime approvedAt;
    private String paymentDate;
    private BigDecimal amount;
    private Boolean isCancelled;
    private BigDecimal cancelAmount;
    private LocalDateTime cancelledAt;
    private String merchantName;
    private String tpbcd;
    private String tpbcdNm;
    private Integer installmentMonth;
    private String currency;
    private BigDecimal foreignAmount;
    private LocalDateTime createdAt;
}
