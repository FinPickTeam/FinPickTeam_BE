package org.scoula.transactions.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CardTransactionDto {
    private Long id;
    private Long userId;
    private Long cardId;
    private String authNumber;
    private String salesType;
    private LocalDateTime approvedAt;
    private BigDecimal amount;
    private String merchantName;
    private String tpbcd;
    private String tpbcdNm;
}
