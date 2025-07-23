package org.scoula.finance.service.stock;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.finance.dto.stock.StockAccessTokenDto;
import org.scoula.finance.dto.stock.StockAccountDto;
import org.scoula.finance.mapper.StockMapper;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Scanner;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StockMapper stockMapper;

    @Value("${stock.api-key}")
    private String apiKey;
    @Value("${stock.api-secret}")
    private String apiSecret;

    @Override
    public StockAccessTokenDto issueAndSaveToken(Long id){
        try{
            //요청할 API URL
            String host = "https://mockapi.kiwoom.com";
            String endpoint = "/oauth2/token";
            String urlString = host + endpoint;

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Header 데이터 설정
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            //JSON 생성
            String jsonData = createJsonData();

            // JSON 데이터 전송
            try(OutputStream os = connection.getOutputStream()){
                byte[] input = jsonData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            StringBuilder response = new StringBuilder();
            try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
            }
            // 토큰, 만료일 저장
            Map<String, String> responseMap = objectMapper.readValue(
                    response.toString(),
                    new TypeReference<Map<String, String>>() {}
            );

            String accessToken = responseMap.get("token");
            String expiresDt = responseMap.get("expires_dt");

            StockAccessTokenDto stockAccessTokenDto = new StockAccessTokenDto(id, "8106-9967", accessToken, expiresDt);
            stockMapper.saveOrUpdateToken(stockAccessTokenDto);

            return stockAccessTokenDto;

        } catch (Exception e){log.error(e.getMessage()); return null;}
    }

//   계좌번호, 수익률 전달
    @Override
    public StockAccountDto getAccountReturnRate(Long id){
        String userAccount =  stockMapper.getUserAccount(id);

        try{
            String host = "https://mockapi.kiwoom.com";
            String endpoint = "/api/dostk/acnt";
            String urlString = host + endpoint;

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            String token = stockMapper.getUserToken(id);

            // Header 데이터 설정
            connection.setRequestMethod("POST"); // 메서드 타입
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8"); // 컨텐츠타입
            connection.setRequestProperty("authorization", "Bearer " + token); // 접근토큰
            connection.setRequestProperty("api-id", "kt00018"); // TR명
            connection.setDoOutput(true);

            // json 설정 및 전송
            String jsonData = "{\"qry_tp\" : \"1\",\"dmst_stex_tp\" : \"KRX\"}";

            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonData.getBytes(StandardCharsets.UTF_8));
            }

            StringBuilder response = new StringBuilder();
            try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
            }
            JsonNode root = objectMapper.readTree(response.toString());
            String totalReturnRateStr = root.path("tot_prft_rt").asText();
            BigDecimal normalizedRate = new BigDecimal(totalReturnRateStr);
            String totalReturnRate = normalizedRate.toPlainString();

            return new StockAccountDto(userAccount, totalReturnRate);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String createJsonData() throws JsonProcessingException {
        Map<String, String> payload = new HashMap<>();
        payload.put("grant_type", "client_credentials");
        payload.put("appkey", apiKey);
        payload.put("secretkey", apiSecret);
        return objectMapper.writeValueAsString(payload);
    }
}
