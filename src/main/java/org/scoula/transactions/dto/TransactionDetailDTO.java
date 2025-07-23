package org.scoula.transactions.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDetailDTO {
    private Long id;
    private Long userId;
    private String place;
    private String category;
    private String type;
    private BigDecimal amount;
    private LocalDateTime date;
    private String memo;
    private String accountName;
    private String accountNumber;
    private String analysisText;
}
