package org.scoula.nhapi.parser;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.scoula.nhapi.dto.NhCardTransactionResponseDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class NhCardParser {

    public static List<NhCardTransactionResponseDto> parse(JSONObject response) {
        // NH 쪽 응답은 "REC"일 수도 있어서 둘 다 대비
        JSONArray recArray = response.optJSONArray("Rec");
        if (recArray == null) recArray = response.optJSONArray("REC");
        if (recArray == null) return List.of();

        List<NhCardTransactionResponseDto> list = new ArrayList<>();

        for (int i = 0; i < recArray.length(); i++) {
            JSONObject obj = recArray.getJSONObject(i);

            // 1) 승인번호: 우선 가능한 키들 탐색, 없으면 결정적 더미 생성
            String auth = opt(obj, "AthzNo", "AuthNo", "AuthNumber", "ApvNo");
            if (isBlank(auth)) auth = deriveAuthFallback(obj);

            // 2) 금액/일시/가맹점: 모의키까지 모두 커버
            BigDecimal amount = parseDecimal(obj, "ApvAmt", "AthzAmt", "Amt", "ApprovedAmt");
            LocalDateTime approvedAt = parseDateTime(opt(obj, "ApvYmdHms", "AthzDtm", "AprvDtm", "ApprovedDateTime", "AthzDateTime"));
            LocalDate paymentDate = parseDate(opt(obj, "StlmDt", "PaymentDate", "Pymd"));
            String merchantName = opt(obj, "MrhstNm", "MctNm", "MerchantName", "MchtNm");

            // 3) 기타 필드
            String salesType = opt(obj, "Stcd", "SalesType", "Sales_Tp");
            boolean cancelled = parseYn(obj, "CancYn", "CnclYn", "IsCancelled");
            BigDecimal cancelAmt = parseDecimal(obj, "CancAmt", "CancelAmount");
            LocalDateTime cancelledAt = parseDateTime(opt(obj, "CancDtm", "CnclDtm", "CancelledDateTime"));

            String tpbcd = opt(obj, "TpbCd", "Tpbcd", "Mcc");
            String tpbcdNm = opt(obj, "TpbCdNm", "TpbcdNm", "MccNm");

            String currency = opt(obj, "FgnCrcyCd", "Currency", "Ccy");
            if (isBlank(currency)) currency = "KRW";

            int instMm = obj.optInt("InstMmCnt", obj.optInt("InstallmentMonth", 0));

            // ====== ✅ NOT NULL 대비 폴백들 ======
            if (approvedAt == null) {
                approvedAt = LocalDateTime.now();                 // ✅ approved_at 보정(컬럼이 NOT NULL일 가능성 대비)
            }
            if (paymentDate == null) {
                paymentDate = approvedAt.toLocalDate().plusDays(2); // ✅ payment_date 보정(가장 중요한 라인)
            }
            if (isBlank(merchantName)) merchantName = "미상";        // ✅ 가맹점 이름 보정
            if (isBlank(salesType)) salesType = "1";                 // ✅ 매출유형 기본값
            if (tpbcdNm == null && tpbcd != null) tpbcdNm = tpbcd;   // ✅ 분류명 없으면 코드로 대체
            // ===================================

            NhCardTransactionResponseDto dto = NhCardTransactionResponseDto.builder()
                    .authNumber(auth)
                    .salesType(salesType)
                    .approvedAt(approvedAt)
                    .paymentDate(paymentDate)
                    .amount(amount != null ? amount : BigDecimal.ZERO)
                    .cancelled(cancelled)
                    .cancelAmount(cancelAmt != null ? cancelAmt : BigDecimal.ZERO)
                    .cancelledAt(cancelledAt)
                    .merchantName(merchantName)
                    .tpbcd(tpbcd)
                    .tpbcdNm(tpbcdNm)
                    .installmentMonth(instMm)
                    .currency(currency)
                    .foreignAmount(parseDecimal(obj, "FgnAthzAmt", "ForeignAmount", "FrnAmt"))
                    .build();

            list.add(dto);
        }

        list.sort(Comparator.comparing(NhCardTransactionResponseDto::getApprovedAt,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return list;
    }


    /* ---------- helpers ---------- */

    private static String opt(JSONObject o, String... keys) {
        for (String k : keys) {
            String v = o.optString(k, null);
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
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
        return null;
    }

    private static LocalDate parseDate(String yyyymmdd) {
        if (yyyymmdd == null || yyyymmdd.isBlank()) return null;
        try {
            String s = yyyymmdd.replaceAll("[^0-9]", "");
            return LocalDate.parse(s, DateTimeFormatter.BASIC_ISO_DATE);
        } catch (Exception e) {
            log.warn("⚠️ 날짜 파싱 실패(yyyyMMdd): {}", yyyymmdd);
            return null;
        }
    }

    private static LocalDateTime parseDateTime(String yyyymmddhhmmss) {
        if (yyyymmddhhmmss == null || yyyymmddhhmmss.isBlank()) return null;
        String s = yyyymmddhhmmss.replaceAll("[^0-9]", ""); // 2025-08-13T19:12:00 → 20250813191200
        try {
            if (s.length() == 8) s += "000000";
            else if (s.length() == 10) s += "0000";
            else if (s.length() == 12) s += "00";
            else if (s.length() > 14) s = s.substring(0, 14);
            return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        } catch (Exception e) {
            log.warn("⚠️ 일시 파싱 실패(yyyyMMddHHmmss): {}", yyyymmddhhmmss);
            return null;
        }
    }

    /**
     * 승인번호가 없을 때 결정적(fixed) 더미 승인번호 생성.
     * 같은 원본 레코드면 항상 같은 값이 나오므로 재동기화/재수집에도 중복 없이 동작.
     */
    private static String deriveAuthFallback(JSONObject o) {
        String stamp = opt(o, "ApvYmdHms", "AthzDtm", "AprvDtm", "ApprovedDateTime", "AthzDateTime");
        String amt   = opt(o, "ApvAmt", "AthzAmt", "Amt", "ApprovedAmt");
        String merch = opt(o, "MrhstNm", "MctNm", "MerchantName", "MchtNm");
        String seed = (stamp == null ? "" : stamp) + "|" + (amt == null ? "" : amt) + "|" + (merch == null ? "" : merch);
        // 해시 → 양수 문자열
        int h = seed.hashCode();
        String suffix = Integer.toUnsignedString(h);
        return "DUM" + suffix; // 예: DUM187a9c2b
    }
}
