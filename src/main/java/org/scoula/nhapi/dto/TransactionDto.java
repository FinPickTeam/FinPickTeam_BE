package org.scoula.nhapi.dto;

import lombok.Builder;
import lombok.Data;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * NH API 거래내역 한 건을 파싱하는 DTO
 */
@Data
@Builder
public class TransactionDto {

    private String place;
    private LocalDateTime date;
    private String type;        // INCOME or EXPENSE
    private BigDecimal amount;
    private String memo;
    private String category;
    private String analysis;

    // ✅ 여기! from(JSONObject) 메서드 정의해야 함
    public static TransactionDto from(JSONObject obj) {
        return TransactionDto.builder()
                .place(obj.getString("Trnm")) // 거래처명
                .date(parseDateTime(obj.getString("Trdd"), obj.getString("Txtm")))
                .type("1".equals(obj.getString("InotDsnc")) ? "INCOME" : "EXPENSE")
                .amount(new BigDecimal(obj.getString("Tram")))
                .memo(obj.optString("Etct", null))
                .category(null)
                .analysis(null)
                .build();
    }

    private static LocalDateTime parseDateTime(String date, String time) {
        return LocalDateTime.parse(date + "T" + time + ":00",
                DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
    }
}
