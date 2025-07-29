package org.scoula.ibkapi.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.scoula.ibkapi.dto.CardDto;
import org.scoula.ibkapi.dto.CardTransactionDto;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@RequiredArgsConstructor
public class IBKCardApiClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 테스트용 인증정보
    private static final String CLIENT_ID = "your-client-id";
    private static final String CLIENT_SECRET = "your-client-secret";
    private static final String OAPI_USER_SRN = "your-oapiusersrn";

    private static final String BASE_URL = "https://devapiportal.ibk.co.kr:9443";

    // 1. 카드 목록 조회
    public List<CardDto> callCardList(String nextKey) {
        String url = BASE_URL + "/ibk/biz/bizOapiIndvCardCtlgInq";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("cardListNextkeyCdn", nextKey != null ? nextKey : "");
        requestBody.put("pageInqNbi", 10);

        HttpHeaders headers = getDefaultHeaders();
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, request, JsonNode.class);
        JsonNode cardList = response.getBody().get("cardList");

        List<CardDto> result = new ArrayList<>();
        if (cardList != null && cardList.isArray()) {
            for (JsonNode card : cardList) {
                CardDto dto = new CardDto();
                dto.setOapiCardAltrNo(card.get("oapiCardAltrNo").asText());
                dto.setCardMaskednum(card.get("cdn").asText());
                dto.setCardName(card.get("rprsPdm").asText());
                dto.setBackCode("003"); // 기업은행 코드 하드코딩
                dto.setBankName("IBK기업은행");
                dto.setCardMemberType("SELF");  // 임의값 (오픈뱅킹 기반)
                dto.setCardType("CREDIT");      // 임의값
                result.add(dto);
            }
        }

        return result;
    }

    // 2. 카드 승인내역 조회
    public List<CardTransactionDto> callTransactionList(String oapiCardAltrNo, String nextApn) {
        String url = BASE_URL + "/ibk/biz/bizOapiAthzHstInq";

        Map<String, Object> body = new HashMap<>();
        body.put("oapiCardAltrNo", oapiCardAltrNo);
        body.put("insgYmd", "20240101");
        body.put("inqFnshYmd", "20241231");
        body.put("athsListNextkeyAthzYmd", "");
        body.put("athsListNextkeyAthzHms", "");
        body.put("athsListNextkeyCdn", "");
        body.put("athsListNextkeyApn", nextApn != null ? nextApn : "");
        body.put("pageInqNbi", 15);

        HttpHeaders headers = getDefaultHeaders();
        headers.set("Authorization", "Bearer your-access-token");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, request, JsonNode.class);

        JsonNode list = response.getBody().get("athsList");
        List<CardTransactionDto> result = new ArrayList<>();

        if (list != null && list.isArray()) {
            for (JsonNode tx : list) {
                CardTransactionDto dto = new CardTransactionDto();
                dto.setAuthNumber(tx.get("apn").asText());
                dto.setApprovedAt(tx.get("athzYmd").asText() + " " + tx.get("athzHms").asText());
                dto.setAmount(tx.get("athzAmt").asDouble());
                dto.setCancelAmount(tx.get("cnclAmt").asDouble());
                dto.setCancelledAt(tx.get("cnclYmd").asText());
                dto.setIsCancelled(dto.getCancelAmount() > 0);
                dto.setMerchantName(tx.get("afstNm").asText());
                dto.setMerchantIndustryCode(tx.has("tpbcd") ? tx.get("tpbcd").asText() : null);
                dto.setMerchantIndustry(tx.has("tpbcdNm") ? tx.get("tpbcdNm").asText() : null);
                dto.setInstallmentMonth(tx.get("inslTrm").asInt());
                dto.setCurrency(tx.get("trnCrcd").asText());
                dto.setForeignAmount(tx.get("atplAthzAmt").asDouble());
                dto.setPurchaseDate(tx.get("bngYmd").asText());
                result.add(dto);
            }
        }

        return result;
    }

    private HttpHeaders getDefaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-IBK-Client-Id", CLIENT_ID);
        headers.set("X-IBK-Client-Secret", CLIENT_SECRET);
        headers.set("oapiusersrn", OAPI_USER_SRN);
        return headers;
    }
}
