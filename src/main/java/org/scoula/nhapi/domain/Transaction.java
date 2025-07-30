package org.scoula.nhapi.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class Transaction {
    private Long id;
    private Long userId;
    private Long accountId;
    private String place;
    private LocalDateTime date;
    private String category;
    private String type;
    private BigDecimal amount;
    private String memo;
    private String analysis;
}
