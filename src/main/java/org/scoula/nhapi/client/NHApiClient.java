package org.scoula.nhapi.client;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.scoula.nhapi.dto.FinAccountRequestDto;
import org.scoula.nhapi.exception.NHApiException;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class NHApiClient {

    private static final String BASE_URL = "https://developers.nonghyup.com";
    private static final String API_KEY = "xxx"; // 실제 API 키 사용

    public JSONObject callOpenFinAccount(FinAccountRequestDto dto) {
        JSONObject body = new JSONObject();
        body.put("Header", buildHeader("OpenFinAccountDirect"));
        body.put("DrtrRgyn", "Y");
        body.put("Bncd", "011");
        body.put("BrdtBrno", dto.getBirthday());
        body.put("Acno", dto.getAccountNumber());

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

    private JSONObject post(String path, JSONObject body) {
        try {
            URL url = new URL(BASE_URL + path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
            throw new NHApiException("NH API 호출 실패: " + path, e);
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
            case "OpenFinAccountDirect", "CheckOpenFinAccountDirect" -> "DrawingTransferA";
            case "InquireBalance", "InquireTransactionHistory" -> "ReceivedTransferA";
            default -> throw new NHApiException("지원하지 않는 ApiNm");
        });
        header.put("IsTuno", UUID.randomUUID().toString().substring(0, 20));
        header.put("AccessToken", API_KEY);
        return header;
    }
}

