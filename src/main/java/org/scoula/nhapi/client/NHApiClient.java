package org.scoula.nhapi.client;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
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
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class NHApiClient {

    // ===== 설정 =====
    private static final String BASE_URL = "https://developers.nonghyup.com";
    private static final String API_KEY  = "DONT-CARE-IN-MOCK";
    private static final boolean ALWAYS_MOCK = true; // ← 무조건 모크

    // ===== 공개 메서드 (서비스에선 이거 그대로 호출) =====
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
        body.put("Cano", cardNumber);
        body.put("Brdt", birthday);
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
        body.put("Insymd", fromDate);
        body.put("Ineymd", toDate);
        body.put("PageNo", String.valueOf(pageNo));
        body.put("Dmcnt", "100");
        return post("/InquireCreditCardAuthorizationHistory.nh", body);
    }

    // ===== 공통 POST: 모크 우선, 실패시에도 모크 =====
    private JSONObject post(String path, JSONObject body) {
        if (ALWAYS_MOCK) return mockFor(path, body); // 네트워크 자체 안 감

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

            String res = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining());
            return new JSONObject(res);

        } catch (Exception e) {
            log.warn("NH 호출 실패 -> mock fallback. path={}, err={}", path, e.getMessage());
            return mockFor(path, body);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // ===== 모크 응답 생성기 (API별 키 형태 맞춤) =====
    private JSONObject mockFor(String path, JSONObject body) {
        String apiNm = body.optJSONObject("Header") != null
                ? body.getJSONObject("Header").optString("ApiNm", "Mock")
                : "Mock";
        JSONObject headerWrap = new JSONObject().put("Header", okHeader(apiNm));

        switch (path) {
            // 계좌: 핀 발급 → Rgno
            case "/OpenFinAccountDirect.nh":
                return headerWrap.put("Rgno", genRgno());

            // 계좌: rgno 확인 → FinAcno
            case "/CheckOpenFinAccountDirect.nh":
                return headerWrap
                        .put("FinAcno", genFin("9"))
                        .put("Bncd", "011");

            // 잔액 조회
            case "/InquireBalance.nh": {
                long bal = 900_000L + rng("bal").nextInt(1_200_000);
                return headerWrap.put("Ldbl", String.valueOf(bal)); // 너네 파서 키에 맞춰 사용
            }

            // 계좌 거래 내역
            case "/InquireTransactionHistory.nh": {
                JSONArray rec = new JSONArray()
                        .put(new JSONObject().put("Trdd","20250812").put("Tram",1500000).put("MnrcDrot","1").put("Rmrk","급여").put("AftrBlnc",1500000))
                        .put(new JSONObject().put("Trdd","20250813").put("Tram", 12000).put("MnrcDrot","2").put("Rmrk","김밥천국").put("AftrBlnc",1488000))
                        .put(new JSONObject().put("Trdd","20250814").put("Tram",300000).put("MnrcDrot","2").put("Rmrk","정기적금").put("AftrBlnc",1188000));
                return headerWrap.put("REC", rec).put("PageNo","1").put("Dmcnt", String.valueOf(rec.length()));
            }

            // 카드: 핀 발급 → Rgno
            case "/OpenFinCardDirect.nh":
                return headerWrap.put("Rgno", genRgno());

            // 카드: rgno 확인 → FinCard
            case "/CheckOpenFinCardDirect.nh":
                return headerWrap.put("FinCard", genFin("8"));

            // 카드 승인 내역
            case "/InquireCreditCardAuthorizationHistory.nh": {
                JSONArray rec = new JSONArray()
                        .put(new JSONObject().put("ApvYmdHms","2025-08-13T19:12:00").put("ApvAmt",18000).put("MrhstNm","맘스터치").put("TpbcdNm","외식/배달").put("SalesType","1").put("CnclYn","N"))
                        .put(new JSONObject().put("ApvYmdHms","2025-08-14T22:05:00").put("ApvAmt", 4200).put("MrhstNm","GS25").put("TpbcdNm","편의점/마트").put("SalesType","1").put("CnclYn","N"));
                return headerWrap.put("REC", rec).put("IsMore","N").put("TotCnt", rec.length());
            }

            default:
                return headerWrap;
        }
    }

    // ===== 헤더/헬퍼 =====
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

    private static JSONObject okHeader(String apiName) {
        return new JSONObject()
                .put("ApiNm", apiName)
                .put("Rpcd", "00000")
                .put("Rsms", "OK");
    }

    private static String genRgno() {
        return "RG" + Long.toString(System.nanoTime(), 36).toUpperCase();
    }

    private static String genFin(String prefix) {
        Random r = rng(prefix, System.nanoTime());
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 1; i < 24; i++) sb.append(r.nextInt(10));
        return sb.toString();
    }

    private static Random rng(Object... seeds) {
        return new Random(Objects.hash(seeds));
    }

    // (원래 쓰던 fallback은 이제 필요 없음. 남겨두고 싶으면 아래처럼 써.)
    private JSONObject createFallbackResponse() {
        JSONObject fallback = new JSONObject();
        fallback.put("Header", new JSONObject().put("Rpcd", "ERROR"));
        fallback.put("Message", "Mock fallback response");
        return fallback;
    }
}
