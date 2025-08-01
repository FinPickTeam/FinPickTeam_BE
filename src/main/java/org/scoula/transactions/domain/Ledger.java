package org.scoula.transactions.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
public class Ledger {
    private Long id;
    private Long userId;
    private Long sourceId;
    private Long accountId;
    private Long cardId;
    private String sourceType; // ACCOUNT or CARD
    private String sourceName;
    private String type; // INCOME or EXPENSE
    private BigDecimal amount;
    private String category;
    private String memo;
    private String analysis;
    private LocalDateTime date;
    private String merchantName;
    private String place;
    private LocalDateTime createdAt;
}
