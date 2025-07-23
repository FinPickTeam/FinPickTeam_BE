package org.scoula.transactions.domain;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {
    private Long id;
    private Long userId;
    private Long accountId;
    private String place;
    private LocalDateTime date;
    private String category;
    private String type; // INCOME or EXPENSE
    private BigDecimal amount;
    private String memo;
}
