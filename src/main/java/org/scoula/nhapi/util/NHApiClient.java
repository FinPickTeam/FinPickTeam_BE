package org.scoula.nhapi.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.scoula.nhapi.domain.Account;
import org.scoula.nhapi.dto.FinAccountRequestDto;
import org.scoula.nhapi.dto.TransactionDto;
import org.scoula.nhapi.exception.NHApiException;
import org.scoula.nhapi.mapper.NhAccountMapper;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class NHApiClient {

    private final NhAccountMapper nhAccountMapper;

    private final String BASE_URL = "https://developers.nonghyup.com";
    private final String API_KEY = "97150a353857633d8d6d1963f03b293ac4d2b4dca758804ed0240896e469b818";

    public String callOpenFinAccount(FinAccountRequestDto dto) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(BASE_URL + "/OpenFinAccountDirect.nh");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            JSONObject body = new JSONObject();
            body.put("Header", buildHeader("OpenFinAccountDirect"));
            body.put("DrtrRgyn", "Y");
            body.put("Bncd", "011");
            body.put("BrdtBrno", dto.getBirthday());
            body.put("Acno", dto.getAccountNumber());

            log.info("📤 [핀어카운트 발급 요청] 바디: {}", body.toString());

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            log.info("📥 [발급 응답 코드]: {}", responseCode);

            InputStream stream = (responseCode >= 400) ? conn.getErrorStream() : conn.getInputStream();
            String response = new BufferedReader(new InputStreamReader(stream))
                    .lines().collect(Collectors.joining());

            log.info("📥 [발급 응답 바디]: {}", response);

            JSONObject json = new JSONObject(response);
            JSONObject header = json.getJSONObject("Header");
            String rpcd = header.getString("Rpcd");

            if ("A0013".equals(rpcd)) {
                log.warn("⚠ 이미 등록된 핀어카운트입니다. DB 조회 시도");
                Account existing = nhAccountMapper.findByAccountNumber(dto.getAccountNumber());
                if (existing == null || existing.getPinAccountNumber() == null) {
                    throw new NHApiException("이미 등록된 계좌지만 DB에 핀어카운트가 없습니다.");
                }
                return existing.getPinAccountNumber();
            }

            if (!"00000".equals(rpcd)) {
                throw new NHApiException("핀어카운트 발급 실패: " + rpcd);
            }

            String rgno = json.getString("Rgno");
            return checkOpenFinAccount(rgno, dto.getBirthday());

        } catch (IOException io) {
            if (conn != null) {
                try (BufferedReader errReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    String error = errReader.lines().collect(Collectors.joining());
                    log.error("💥 [발급 요청 IOException] 에러 바디: {}", error);
                } catch (Exception ignore) {}
            }
            throw new NHApiException("핀어카운트 API 호출 실패", io);
        } catch (Exception e) {
            log.error("💥 [발급 요청 실패]", e);
            throw new NHApiException("핀어카운트 API 호출 실패", e);
        }
    }


    public String checkOpenFinAccount(String rgno, String birthday) {
        try {
            URL url = new URL(BASE_URL + "/CheckOpenFinAccountDirect.nh");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            JSONObject body = new JSONObject();
            body.put("Header", buildHeader("CheckOpenFinAccountDirect"));
            body.put("Rgno", rgno);
            body.put("BrdtBrno", birthday);

            log.info("📤 [핀어카운트 확인 요청] 바디: {}", body.toString());

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            log.info("📥 [확인 응답 코드]: {}", responseCode);

            String response = new BufferedReader(new InputStreamReader(conn.getInputStream()))
                    .lines().collect(Collectors.joining());

            log.info("📥 [확인 응답 바디]: {}", response);

            JSONObject json = new JSONObject(response);
            JSONObject header = json.getJSONObject("Header");
            String rpcd = header.getString("Rpcd");
            if (!"00000".equals(rpcd)) {
                throw new NHApiException("핀어카운트 확인 실패: " + rpcd);
            }

            return json.getString("FinAcno");

        } catch (Exception e) {
            log.error("💥 [핀어카운트 확인 실패]", e);
            throw new NHApiException("핀어카운트 확인 API 호출 실패", e);
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
            case "CheckOpenFinAccountDirect" -> "DrawingTransferA";
            case "OpenFinAccountDirect" -> "DrawingTransferA";
            case "InquireBalance", "InquireTransactionHistory" -> "ReceivedTransferA";
            default -> throw new NHApiException("지원하지 않는 ApiNm: " + apiName);
        });
        header.put("IsTuno", UUID.randomUUID().toString().substring(0, 20));
        header.put("AccessToken", API_KEY);
        return header;
    }

    public BigDecimal callInquireBalance(String finAcno) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(BASE_URL + "/InquireBalance.nh");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            JSONObject body = new JSONObject();
            body.put("Header", buildHeader("InquireBalance"));
            body.put("FinAcno", finAcno);

            log.info("📤 잔액 요청 바디: {}", body.toString());

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            log.info("📥 응답 코드: {}", responseCode);

            InputStream stream;
            if (responseCode >= 400) {
                stream = conn.getErrorStream();
            } else {
                stream = conn.getInputStream();
            }

            String response = new BufferedReader(new InputStreamReader(stream))
                    .lines().collect(Collectors.joining());

            log.info("📥 응답 바디: {}", response);

            JSONObject json = new JSONObject(response);
            String rpcd = json.getJSONObject("Header").getString("Rpcd");
            if (!"00000".equals(rpcd)) {
                throw new NHApiException("잔액 조회 실패: " + rpcd);
            }

            return new BigDecimal(json.getString("Ldbl"));
        } catch (Exception e) {
            log.error("💥 잔액 조회 실패", e);
            throw new NHApiException("잔액 조회 API 실패", e);
        }
    }


    public List<TransactionDto> callTransactionList(String finAcno, String fromDate, String toDate) {
        try {
            URL url = new URL(BASE_URL + "/InquireTransactionHistory.nh");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            JSONObject body = new JSONObject();
            body.put("Header", buildHeader("InquireTransactionHistory"));
            body.put("FinAcno", finAcno);
            body.put("Insymd", fromDate);
            body.put("Ineymd", toDate);
            body.put("TrnsDsnc", "A");
            body.put("Lnsq", "1");
            body.put("PageNo", "1");
            body.put("Bncd", "011");
            body.put("Dmcnt", "100");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }

            String res = new BufferedReader(new InputStreamReader(conn.getInputStream()))
                    .lines().collect(Collectors.joining());

            JSONObject json = new JSONObject(res);
            String rpcd = json.getJSONObject("Header").getString("Rpcd");

            // ✅ 거래내역 없음 A0090 → 빈 리스트 리턴
            if ("A0090".equals(rpcd)) {
                log.warn("⚠ 거래내역 없음 (Rpcd=A0090)");
                return new ArrayList<>();
            }

            if (!"00000".equals(rpcd)) {
                throw new NHApiException("거래내역 실패: " + rpcd);
            }

            JSONArray arr = json.getJSONArray("Rec");
            List<TransactionDto> result = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                result.add(TransactionDto.builder()
                        .place(obj.getString("Trnm"))
                        .date(LocalDateTime.parse(obj.getString("Trdd") + "T" + obj.getString("Txtm") + ":00"))
                        .type(obj.getString("InotDsnc").equals("1") ? "INCOME" : "EXPENSE")
                        .amount(new BigDecimal(obj.getString("Tram")))
                        .memo(obj.optString("Etct", null))
                        .category(null)
                        .analysis(null)
                        .build());
            }
            return result;

        } catch (Exception e) {
            log.error("💥 거래내역 API 실패", e);
            throw new NHApiException("거래내역 API 실패", e);
        }
    }



}
