package org.scoula.nhapi.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BalanceResponseDto {
    private String finAccount;
    private BigDecimal balance;
}
