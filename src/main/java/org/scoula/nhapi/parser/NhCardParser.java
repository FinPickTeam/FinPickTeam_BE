package org.scoula.nhapi.parser;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.scoula.nhapi.dto.NhCardTransactionResponseDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class NhCardParser {

    public static List<NhCardTransactionResponseDto> parse(JSONObject response) {
        JSONArray recArray = response.optJSONArray("Rec");
        if (recArray == null) return List.of();

        List<NhCardTransactionResponseDto> list = new ArrayList<>();

        for (int i = 0; i < recArray.length(); i++) {
            JSONObject obj = recArray.getJSONObject(i);

            NhCardTransactionResponseDto dto = new NhCardTransactionResponseDto();
            dto.setAuthNumber(obj.optString("AthzNo", null));
            dto.setSalesType(obj.optString("Stcd", null));
            dto.setApprovedAt(obj.optString("AthzDtm", null));
            dto.setPaymentDate(obj.optString("StlmDt", null));
            dto.setAmount(parseDecimal(obj, "AthzAmt"));
            dto.setCancelled("Y".equalsIgnoreCase(obj.optString("CancYn")));
            dto.setCancelAmount(parseDecimal(obj, "CancAmt"));
            dto.setCancelledAt(obj.optString("CancDtm", null));
            dto.setMerchantName(obj.optString("MctNm", null));
            dto.setTpbcd(obj.optString("TpbCd", null));
            dto.setTpbcdNm(obj.optString("TpbCdNm", null));
            dto.setInstallmentMonth(obj.optInt("InstMmCnt", 0));
            dto.setCurrency(obj.optString("FgnCrcyCd", null));
            dto.setForeignAmount(parseDecimal(obj, "FgnAthzAmt"));

            list.add(dto);
        }

        return list;
    }

    private static BigDecimal parseDecimal(JSONObject obj, String key) {
        try {
            String raw = obj.optString(key, "0").replace(",", "").trim();
            return raw.isEmpty() ? BigDecimal.ZERO : new BigDecimal(raw);
        } catch (Exception e) {
            log.warn("⚠️ 금액 파싱 실패 [{}]: {}", key, obj.optString(key));
            return BigDecimal.ZERO;
        }
    }

}
