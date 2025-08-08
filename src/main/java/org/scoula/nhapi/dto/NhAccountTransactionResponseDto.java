package org.scoula.nhapi.dto;

import lombok.Builder;
import lombok.Data;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
public class NhAccountTransactionResponseDto {
    private Long userId;
    private Long accountId;
    private LocalDateTime date;
    private String type; // INCOME or EXPENSE
    private BigDecimal amount;
    private BigDecimal balance;
    private String place;
    private boolean isCancelled;
    private Long tuNo;
    private String memo;
    private String category;
    private String analysis;

    public static NhAccountTransactionResponseDto from(JSONObject obj) {
        return NhAccountTransactionResponseDto.builder()
                .date(parseDateTime(obj.getString("Trdd"), obj.getString("Txtm")))
                .type(parseType(obj.optString("MnrcDrotDsnc")))
                .amount(new BigDecimal(obj.getString("Tram")))
                .balance(new BigDecimal(obj.optString("AftrBlnc", "0")))
                .place(obj.optString("Smr", ""))
                .isCancelled("1".equals(obj.optString("Ccyn")))
                .tuNo(generateTuNo(obj))
                .memo(obj.optString("Etct", null))
                .category(null)
                .analysis(null)
                .build();
    }

    private static LocalDateTime parseDateTime(String date, String time) {
        try {
            return LocalDateTime.parse(date + "T" + time + ":00",
                    DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private static String parseType(String code) {
        return switch (code) {
            case "1", "2" -> "INCOME";
            case "3", "4" -> "EXPENSE";
            default -> "EXPENSE";
        };
    }

    private static Long generateTuNo(JSONObject obj) {
        String key = obj.optString("Trdd") + obj.optString("Txtm")
                + obj.optString("Tram") + obj.optString("Trnm");
        return (long) key.hashCode();
    }

    public boolean isIncome() {
        return "INCOME".equals(this.type);
    }
}
