package org.scoula.nhapi.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionDto {
    private Long userId;
    private Long accountId;
    private String place;
    private LocalDateTime date;
    private String category;
    private String type; // INCOME or EXPENSE
    private BigDecimal amount;
    private String memo;
    private String analysis;
}
