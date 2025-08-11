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

    // NH JSON → DTO (기존)
    public static NhAccountTransactionResponseDto from(JSONObject obj) {
        return NhAccountTransactionResponseDto.builder()
                .date(parseDateTime(obj.optString("Trdd"), obj.optString("Txtm")))
                .type(parseType(obj.optString("MnrcDrotDsnc")))
                .amount(new BigDecimal(obj.optString("Tram", "0")))
                .balance(new BigDecimal(obj.optString("AftrBlnc", "0")))
                .place(obj.optString("Smr", ""))
                .isCancelled("1".equals(obj.optString("Ccyn")))
                .tuNo(generateTuNo(obj)) // 기본 해시
                .memo(obj.optString("Etct", null))
                .category(null)
                .analysis(null)
                .build();
    }

    // NH JSON → DTO (userId/accountId 세팅하고 싶을 때 쓰는 오버로드)
    public static NhAccountTransactionResponseDto from(JSONObject obj, Long userId, Long accountId) {
        NhAccountTransactionResponseDto dto = from(obj);
        dto.setUserId(userId);
        dto.setAccountId(accountId);
        // 계좌 단위로 tuNo 범위를 더 안전하게 하고 싶으면 아래 한 줄 켜기
        // dto.setTuNo(mixTuNoWithAccount(dto.getTuNo(), accountId));
        return dto;
    }

    private static LocalDateTime parseDateTime(String yyyymmdd, String hhmmOrHhmmss) {
        try {
            String date = (yyyymmdd != null && yyyymmdd.length() == 8) ? yyyymmdd :
                    java.time.LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

            String time = (hhmmOrHhmmss == null) ? "000000" : hhmmOrHhmmss.trim();
            // HHmm → HHmmss 로 보정, HH → HHmmss 도 보정
            if (time.length() == 4) time = time + "00";
            else if (time.length() == 2) time = time + "0000";
            else if (time.length() < 6) time = String.format("%-6s", time).replace(' ', '0');
            else if (time.length() > 6) time = time.substring(0, 6);

            return LocalDateTime.parse(date + time, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private static String parseType(String code) {
        // NH 코드 매핑(입금/출금 코드 케이스 추가 가능)
        return switch (code) {
            case "1", "2", "C", "CR" -> "INCOME";
            case "3", "4", "D", "DR" -> "EXPENSE";
            default -> "EXPENSE";
        };
    }

    private static Long generateTuNo(JSONObject obj) {
        // 날짜/시간/금액/거래처를 이용한 안정 해시 → long
        String key = obj.optString("Trdd") + obj.optString("Txtm")
                + obj.optString("Tram") + obj.optString("Trnm", obj.optString("Smr", ""));
        return toPositiveLongHash(key);
    }

    // 계좌별로 tuNo 범위를 분리하고 싶을 때 사용
    private static Long mixTuNoWithAccount(Long base, Long accountId) {
        long a = (accountId == null) ? 0L : accountId;
        return (a * 1_000_000_007L) ^ base; // 간단한 도메인 분리
    }

    private static long toPositiveLongHash(String s) {
        long h = 1125899906842597L; // prime seed
        for (int i = 0; i < s.length(); i++) {
            h = 31*h + s.charAt(i);
        }
        return h == Long.MIN_VALUE ? 0 : Math.abs(h);
    }

    public boolean isIncome() {
        return "INCOME".equals(this.type);
    }
}
