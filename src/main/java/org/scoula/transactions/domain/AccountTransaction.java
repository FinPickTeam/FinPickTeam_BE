package org.scoula.transactions.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.scoula.nhapi.dto.NhTransactionResponseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountTransaction {
    private Long id;
    private Long userId;
    private Long accountId;
    private LocalDateTime date;
    private String type;
    private BigDecimal amount;
    private BigDecimal balance;
    private String place;
    private Boolean isCancelled;
    private Long tuNo;

    // ✅ NH DTO → 도메인 변환용 생성자
    public AccountTransaction(NhTransactionResponseDto dto, Long userId, Long accountId) {
        this.userId = userId;
        this.accountId = accountId;
        this.date = dto.getDate();
        this.type = dto.getType();
        this.amount = dto.getAmount();
        this.balance = dto.getBalance();
        this.place = dto.getPlace();
        this.tuNo = dto.getTuNo();
        this.isCancelled = dto.isCancelled();
    }
}

