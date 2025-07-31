package org.scoula.transactions.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountTransactionDto {
    private Long id;
    private Long userId;
    private Long accountId;
    private LocalDateTime date;
    private String type; // INCOME or EXPENSE
    private BigDecimal amount;
    private BigDecimal balance;
    private String place;
}
