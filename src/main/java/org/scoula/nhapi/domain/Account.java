package org.scoula.nhapi.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class Account {
    private Long id;
    private Long userId;
    private String bankName;
    private String accountNumber;
    private String productName;
    private Float interestRate;
    private String pinAccountNumber;
    private String accountType;
    private BigDecimal balance;
    private LocalDateTime connectedAt;
}
