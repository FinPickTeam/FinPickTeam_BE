package org.scoula.transactions.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LedgerDetailDto {
    private Long id;
    private Long userId;
    private String sourceType;
    private String sourceName;
    private BigDecimal amount;
    private String type;
    private String category;
    private String memo;
    private String analysis;
    private LocalDateTime date;
    private String merchantName;
    private String place;
    private Long accountId;
    private Long cardId;
}
