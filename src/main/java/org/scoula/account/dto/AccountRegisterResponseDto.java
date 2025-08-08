package org.scoula.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRegisterResponseDto {

    private Long accountId;         // 등록된 계좌의 고유 ID
    private String finAccount;      // 발급된 핀어카운트 번호
    private BigDecimal balance;     // 현재 계좌 잔액
}
