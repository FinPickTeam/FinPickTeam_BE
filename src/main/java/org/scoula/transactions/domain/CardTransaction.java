package org.scoula.transactions.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.nhapi.dto.NhCardTransactionResponseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Data
@NoArgsConstructor
public class CardTransaction {
    private Long id;
    private Long userId;
    private Long cardId;
    private String authNumber;
    private String salesType;
    private LocalDateTime approvedAt;
    private String paymentDate;
    private BigDecimal amount;
    private Boolean isCancelled;
    private BigDecimal cancelAmount;
    private LocalDateTime cancelledAt;
    private String merchantName;
    private String tpbcd; // 업종 코드
    private String tpbcdNm;
    private Integer installmentMonth;
    private String currency;
    private BigDecimal foreignAmount;
    private LocalDateTime createdAt;

    public CardTransaction(NhCardTransactionResponseDto dto, Long userId, Long cardId) {
        this.userId = userId;
        this.cardId = cardId;
        this.authNumber = dto.getAuthNumber();
        this.salesType = dto.getSalesType();
        this.approvedAt = parseDateTime(dto.getApprovedAt());
        this.paymentDate = dto.getPaymentDate(); // 문자열 그대로 저장
        this.amount = dto.getAmount();
        this.isCancelled = dto.isCancelled();
        this.cancelAmount = dto.getCancelAmount();
        this.cancelledAt = parseDateTime(dto.getCancelledAt());
        this.merchantName = dto.getMerchantName();
        this.tpbcd = dto.getTpbcd();
        this.tpbcdNm = dto.getTpbcdNm();
        this.installmentMonth = dto.getInstallmentMonth();
        this.currency = dto.getCurrency();
        this.foreignAmount = dto.getForeignAmount();
        this.createdAt = LocalDateTime.now();
    }

    private LocalDateTime parseDateTime(String str) {
        if (str == null || str.isEmpty()) return null;
        try {
            str = str.replace("T", ""); // ✅ T 제거 (예: "20250425T120000" → "20250425120000")
            return LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        } catch (Exception e) {
            log.warn("❌ approved_at 날짜 파싱 실패: {}", str); // 로그 남기자
            return null;
        }
    }



}
