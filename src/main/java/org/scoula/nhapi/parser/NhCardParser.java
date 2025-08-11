package org.scoula.nhapi.parser;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.scoula.nhapi.dto.NhCardTransactionResponseDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class NhCardParser {

    public static List<NhCardTransactionResponseDto> parse(JSONObject response) {
        JSONArray recArray = response.optJSONArray("Rec");
        if (recArray == null) return List.of();

        List<NhCardTransactionResponseDto> list = new ArrayList<>();

        for (int i = 0; i < recArray.length(); i++) {
            JSONObject obj = recArray.getJSONObject(i);

            NhCardTransactionResponseDto dto = NhCardTransactionResponseDto.builder()
                    .authNumber(opt(obj, "AthzNo", "AuthNo", "AuthNumber"))
                    .salesType(opt(obj, "Stcd", "SalesType", "Sales_Tp"))
                    .approvedAt(parseDateTime(opt(obj, "AthzDtm", "AprvDtm", "ApprovedDateTime", "AthzDateTime")))
                    .paymentDate(parseDate(opt(obj, "StlmDt", "PaymentDate", "Pymd")))
                    .amount(parseDecimal(obj, "AthzAmt", "Amt", "ApprovedAmt"))
                    .cancelled(parseYn(obj, "CancYn", "CnclYn", "IsCancelled"))
                    .cancelAmount(parseDecimal(obj, "CancAmt", "CancelAmount"))
                    .cancelledAt(parseDateTime(opt(obj, "CancDtm", "CnclDtm", "CancelledDateTime")))
                    .merchantName(opt(obj, "MctNm", "MerchantName", "MchtNm"))
                    .tpbcd(opt(obj, "TpbCd", "Tpbcd", "Mcc"))
                    .tpbcdNm(opt(obj, "TpbCdNm", "TpbcdNm", "MccNm"))
                    .installmentMonth(obj.optInt("InstMmCnt", obj.optInt("InstallmentMonth", 0)))
                    .currency(opt(obj, "FgnCrcyCd", "Currency", "Ccy", "KRW"))
                    .foreignAmount(parseDecimal(obj, "FgnAthzAmt", "ForeignAmount", "FrnAmt"))
                    .build();

            list.add(dto);
        }

        // 시간 정렬 보장(승인일시가 없다면 그대로)
        list.sort(Comparator.comparing(NhCardTransactionResponseDto::getApprovedAt,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return list;
    }

    /* ---------- helpers ---------- */

    private static String opt(JSONObject o, String... keys) {
        for (String k : keys) {
            String v = o.optString(k, null);
            if (v != null && !v.isEmpty()) return v;
        }
        return null;
    }

    private static boolean parseYn(JSONObject o, String... keys) {
        for (String k : keys) {
            if (!o.has(k)) continue;
            String v = String.valueOf(o.opt(k));
            if (v == null) continue;
            v = v.trim();
            if (v.equalsIgnoreCase("Y") || v.equals("1") || v.equalsIgnoreCase("true")) return true;
            if (v.equalsIgnoreCase("N") || v.equals("0") || v.equalsIgnoreCase("false")) return false;
        }
        return false;
    }

    private static BigDecimal parseDecimal(JSONObject o, String... keys) {
        for (String k : keys) {
            String raw = o.optString(k, null);
            if (raw == null || raw.isBlank()) continue;
            try {
                return new BigDecimal(raw.replace(",", "").trim());
            } catch (Exception e) {
                log.warn("⚠️ 금액 파싱 실패 [{}]: {}", k, raw);
            }
        }
        return BigDecimal.ZERO;
    }

    private static LocalDate parseDate(String yyyymmdd) {
        if (yyyymmdd == null || yyyymmdd.isBlank()) return null;
        try {
            return LocalDate.parse(yyyymmdd.replaceAll("[^0-9]", ""), DateTimeFormatter.BASIC_ISO_DATE);
        } catch (Exception e) {
            log.warn("⚠️ 날짜 파싱 실패(yyyyMMdd): {}", yyyymmdd);
            return null;
        }
    }

    private static LocalDateTime parseDateTime(String yyyymmddhhmmss) {
        if (yyyymmddhhmmss == null || yyyymmddhhmmss.isBlank()) return null;
        String s = yyyymmddhhmmss.replaceAll("[^0-9]", ""); // 20250101T120000 → 20250101120000
        try {
            if (s.length() == 8) s += "000000";       // yyyyMMdd → +000000
            else if (s.length() == 10) s += "0000";   // yyyyMMddHH → +mmss
            else if (s.length() == 12) s += "00";     // yyyyMMddHHmm → +ss
            else if (s.length() > 14) s = s.substring(0, 14);
            return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        } catch (Exception e) {
            log.warn("⚠️ 일시 파싱 실패(yyyyMMddHHmmss): {}", yyyymmddhhmmss);
            return null;
        }
    }
}
