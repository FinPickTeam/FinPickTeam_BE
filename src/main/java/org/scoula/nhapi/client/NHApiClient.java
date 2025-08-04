package org.scoula.nhapi.client;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.scoula.nhapi.exception.NHApiException;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class NHApiClient {

    private static final String BASE_URL = "https://developers.nonghyup.com";
    private static final String API_KEY = "97150a353857633d8d6d1963f03b293ac4d2b4dca758804ed0240896e469b818"; // 실제 사용 시 교체

    public JSONObject callOpenFinAccount(String accountNumber, String birthday) {
        JSONObject body = new JSONObject();
        body.put("Header", buildHeader("OpenFinAccountDirect"));
        body.put("DrtrRgyn", "Y");
        body.put("Bncd", "011");
        body.put("BrdtBrno", birthday);
        body.put("Acno", accountNumber);
        return post("/OpenFinAccountDirect.nh", body);
    }

    public JSONObject callCheckFinAccount(String rgno, String birthday) {
        JSONObject body = new JSONObject();
        body.put("Header", buildHeader("CheckOpenFinAccountDirect"));
        body.put("Rgno", rgno);
        body.put("BrdtBrno", birthday);
        return post("/CheckOpenFinAccountDirect.nh", body);
    }

    public JSONObject callInquireBalance(String finAcno) {
        JSONObject body = new JSONObject();
        body.put("Header", buildHeader("InquireBalance"));
        body.put("FinAcno", finAcno);
        return post("/InquireBalance.nh", body);
    }

    public JSONObject callTransactionList(String finAcno, String from, String to) {
        JSONObject body = new JSONObject();
        body.put("Header", buildHeader("InquireTransactionHistory"));
        body.put("FinAcno", finAcno);
        body.put("Insymd", from);
        body.put("Ineymd", to);
        body.put("TrnsDsnc", "A");
        body.put("Lnsq", "1");
        body.put("PageNo", "1");
        body.put("Bncd", "011");
        body.put("Dmcnt", "100");
        return post("/InquireTransactionHistory.nh", body);
    }

    public JSONObject callOpenFinCard(String cardNumber, String birthday) {
        JSONObject body = new JSONObject();
        body.put("Header", buildHeader("OpenFinCardDirect"));
        body.put("CardNo", cardNumber);
        body.put("BrdtBrno", birthday);
        return post("/OpenFinCardDirect.nh", body);
    }

    public JSONObject checkOpenFinCard(String rgno, String birthday) {
        JSONObject body = new JSONObject();
        body.put("Header", buildHeader("CheckOpenFinCardDirect"));
        body.put("Rgno", rgno);
        body.put("Brdt", birthday);
        return post("/CheckOpenFinCardDirect.nh", body);
    }

    public JSONObject callCardTransactionList(String finCard, String fromDate, String toDate, int pageNo) {
        JSONObject body = new JSONObject();
        body.put("Header", buildHeader("InquireCreditCardAuthorizationHistory"));
        body.put("FinCard", finCard);
        body.put("IousDsnc", "1"); // 고정값
        body.put("Insymd", fromDate);  // 조회 시작일
        body.put("Ineymd", toDate);    // 조회 종료일
        body.put("PageNo", String.valueOf(pageNo));
        body.put("Dmcnt", "100"); // 한 번에 100건
        return post("/InquireCreditCardAuthorizationHistory.nh", body);
    }



    private JSONObject post(String path, JSONObject body) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(BASE_URL + path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }

            String res = new BufferedReader(new InputStreamReader(conn.getInputStream()))
                    .lines().collect(Collectors.joining());
            return new JSONObject(res);

        } catch (Exception e) {
            log.error("❌ NH API 호출 실패 (Post 요청) - 경로: {}, 에러: {}", path, e.getMessage());
            // 예외 발생 시 기본 응답
            return createFallbackResponse();
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private JSONObject buildHeader(String apiName) {
        JSONObject header = new JSONObject();
        header.put("ApiNm", apiName);
        header.put("Tsymd", LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        header.put("Trtm", LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
        header.put("Iscd", "003131");
        header.put("FintechApsno", "001");
        header.put("ApiSvcCd", switch (apiName) {
            case "OpenFinAccountDirect", "CheckOpenFinAccountDirect", "OpenFinCardDirect", "CheckOpenFinCardDirect" -> "DrawingTransferA";
            case "InquireBalance", "InquireTransactionHistory" -> "ReceivedTransferA";
            case "InquireCreditCardAuthorizationHistory" -> "CardInfo";
            default -> throw new NHApiException("지원하지 않는 ApiNm: " + apiName);
        });
        header.put("IsTuno", UUID.randomUUID().toString().substring(0, 20));
        header.put("AccessToken", API_KEY);
        return header;
    }

    private JSONObject createFallbackResponse() {
        JSONObject fallback = new JSONObject();
        fallback.put("Header", new JSONObject().put("Rpcd", "ERROR"));
        fallback.put("Message", "Mock fallback response");
        return fallback;
    }
}