package org.scoula.nhapi.dto;

import lombok.Builder;
import lombok.Data;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
public class NhCardTransactionResponseDto {
    private String authNumber;           // 승인번호
    private String salesType;            // 1/2/3/6/7/8
    private LocalDateTime approvedAt;    // 승인일시
    private LocalDate paymentDate;       // 결제일(청구일)
    private BigDecimal amount;           // 승인금액
    private boolean cancelled;           // 취소 여부
    private BigDecimal cancelAmount;     // 취소금액
    private LocalDateTime cancelledAt;   // 취소일시
    private String merchantName;         // 가맹점명
    private String tpbcd;                // 업종코드
    private String tpbcdNm;              // 업종명
    private int installmentMonth;        // 할부개월
    private String currency;             // 통화
    private BigDecimal foreignAmount;    // 해외승인금액

    // NH JSON → DTO 변환(필드명이 다를 수 있어 optString 사용)
    public static NhCardTransactionResponseDto from(JSONObject obj) {
        return NhCardTransactionResponseDto.builder()
                .authNumber(obj.optString("AuthNo", obj.optString("AuthNumber", null)))
                .salesType(obj.optString("SalesType", obj.optString("Sales_Tp", "1")))
                .approvedAt(parseDateTime(
                        obj.optString("AprvDate", obj.optString("ApprovedDate", "")),
                        obj.optString("AprvTime", obj.optString("ApprovedTime", ""))
                ))
                .paymentDate(parseDate(obj.optString("Pymd", obj.optString("PaymentDate", ""))))
                .amount(new BigDecimal(obj.optString("Amt", obj.optString("Amount", "0"))))
                .cancelled("1".equals(obj.optString("CnclYn", obj.optString("IsCancelled", "0")))
                        || obj.optBoolean("IsCancelled", false))
                .cancelAmount(new BigDecimal(obj.optString("CnclAmt", obj.optString("CancelAmount", "0"))))
                .cancelledAt(parseDateTime(
                        obj.optString("CnclDate", obj.optString("CancelledDate", "")),
                        obj.optString("CnclTime", obj.optString("CancelledTime", ""))
                ))
                .merchantName(obj.optString("MchtNm", obj.optString("MerchantName", "")))
                .tpbcd(obj.optString("Tpbcd", obj.optString("Mcc", "")))
                .tpbcdNm(obj.optString("TpbcdNm", obj.optString("MccNm", "")))
                .installmentMonth(obj.optInt("InstMm", obj.optInt("InstallmentMonth", 0)))
                .currency(obj.optString("Ccy", obj.optString("Currency", "KRW")))
                .foreignAmount(new BigDecimal(obj.optString("FrnAmt", obj.optString("ForeignAmount", "0"))))
                .build();
    }

    private static LocalDateTime parseDateTime(String yyyymmdd, String hhmmss) {
        try {
            String d = (yyyymmdd != null && yyyymmdd.length() == 8) ? yyyymmdd : "19700101";
            String t = (hhmmss == null) ? "000000" : hhmmss.replaceAll("[^0-9]", "");
            if (t.length() == 4) t += "00";
            else if (t.length() == 2) t += "0000";
            else if (t.length() < 6) t = String.format("%-6s", t).replace(' ', '0');
            else if (t.length() > 6) t = t.substring(0, 6);
            return LocalDateTime.parse(d + t, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalDate parseDate(String yyyymmdd) {
        try {
            return LocalDate.parse(yyyymmdd, DateTimeFormatter.BASIC_ISO_DATE);
        } catch (Exception e) {
            return null;
        }
    }
}
