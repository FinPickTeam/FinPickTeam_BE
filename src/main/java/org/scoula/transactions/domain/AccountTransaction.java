package org.scoula.transactions.domain;

import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AccountTransaction {
    private Long id;
    private Long userId;
    private Long accountId;
    private LocalDateTime date;
    private String type; // INCOME or EXPENSE
    private BigDecimal amount;
    private BigDecimal balance;
    private String place;
    private Boolean isCancelled;
    private Long tuNo;
}
