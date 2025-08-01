package org.scoula.transactions.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class LedgerDto {
    private Long id;
    private String sourceType;
    private String sourceName;
    private BigDecimal amount;
    private String type;
    private String category;       // 조인된 tr_category.label
    private LocalDateTime date;
    private String merchantName;
}
