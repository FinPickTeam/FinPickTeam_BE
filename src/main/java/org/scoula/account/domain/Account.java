package org.scoula.account.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    private Long id;                    // 계좌 고유 ID (PK)
    private Long userId;               // 사용자 ID (FK)
    private String pinAccountNumber;   // 핀어카운트
    private String bankCode;           // 은행 코드 (예: 011)
    private String accountNumber;      // 계좌번호
    private String productName;        // 상품명
    private String accountType;        // DEPOSIT / SAVINGS 등
    private BigDecimal balance;        // 현재 잔액
    private LocalDateTime createdAt;   // 생성일시 (DB default now())
}
