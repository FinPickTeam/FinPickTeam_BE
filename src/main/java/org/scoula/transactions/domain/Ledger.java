package org.scoula.transactions.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ledger {
    private Long id;
    private Long userId;
    private Long sourceId;
    private Long accountId;
    private Long cardId;
    private String sourceType; // ACCOUNT or CARD
    private String sourceName;
    private String type; // INCOME or EXPENSE
    private BigDecimal amount;
    private String category; //조회 전용 필드
    private Integer categoryId;
    private String memo;
    private String analysis;
    private LocalDateTime date;
    private String merchantName;
    private String place;
    private LocalDateTime createdAt;

    // ✅ 계좌 거래 전용 생성자
    public Ledger(AccountTransaction tx, String sourceName, int categoryId) {
        this.userId = tx.getUserId();
        this.sourceId = tx.getId();
        this.accountId = tx.getAccountId();
        this.sourceType = "ACCOUNT";
        this.sourceName = sourceName;
        this.type = "INCOME";
        this.amount = tx.getAmount();
        this.categoryId = categoryId; // ✅ 고정된 "이체" categoryId 값
        this.memo = null;
        this.analysis = null;
        this.date = tx.getDate();
        this.place = tx.getPlace();

    }
}

