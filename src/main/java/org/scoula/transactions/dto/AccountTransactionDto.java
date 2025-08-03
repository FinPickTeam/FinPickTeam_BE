package org.scoula.transactions.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONObject;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class AccountTransactionDto {
    private Long id;
    private Long userId;
    private Long accountId;
    private LocalDateTime date;
    private String type; // INCOME or EXPENSE
    private BigDecimal amount;
    private BigDecimal balance;
    private String place;
    private Long tuNo;
    private boolean isCancelled;

    // ✅ 여기 안에 from() 메서드 같이 넣기
    public static AccountTransactionDto from(JSONObject obj) {
        return AccountTransactionDto.builder()
                .date(parseDateTime(obj.getString("Trdd"), obj.getString("Txtm")))
                .type(parseType(obj.getString("MnrcDrotDsnc"))) // 입출금 구분 → INCOME/EXPENSE
                .amount(new BigDecimal(obj.getString("Tram")))
                .balance(new BigDecimal(obj.getString("AftrBlnc")))
                .place(obj.optString("Smr", ""))
                .tuNo(Long.parseLong(obj.getString("Tuno")))
                .isCancelled("1".equals(obj.optString("Ccyn")))
                .build();
    }

    private static LocalDateTime parseDateTime(String date, String time) {
        return LocalDateTime.of(
                LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE),
                LocalTime.parse(time, DateTimeFormatter.ofPattern("HHmmss"))
        );
    }

    private static String parseType(String code) {
        return switch (code) {
            case "1", "2" -> "INCOME";
            case "3", "4" -> "EXPENSE";
            default -> "EXPENSE";
        };
    }
}
