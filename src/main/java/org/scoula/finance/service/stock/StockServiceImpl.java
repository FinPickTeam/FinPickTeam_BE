package org.scoula.finance.service.stock;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.finance.dto.stock.StockAccessTokenDto;
import org.scoula.finance.dto.stock.StockAccountDto;
import org.scoula.finance.dto.stock.StockDetailDto;
import org.scoula.finance.dto.stock.StockListDto;
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
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

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
            String endpoint = "/oauth2/token";

            URL url = new URL(createUrl(endpoint));
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
            String endpoint = "/api/dostk/acnt";

            URL url = new URL(createUrl(endpoint));
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

    @Override
    public List<StockListDto> getStockList(Long id){

    }


    // 주식 상세 정보 조회
    @Override
    public StockDetailDto getStockDetail(Long id, String stockCode){
        String endpoint = "/api/dostk/stkinfo";

        try{
            URL url = new URL(createUrl(endpoint));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            String token = stockMapper.getUserToken(id);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("authorization", "Bearer " + token);
            connection.setRequestProperty("api-id", "ka10001");
            connection.setDoOutput(true);

            String jsonData = String.format("{\"stk_cd\" : \"%s\"}", stockCode);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonData.getBytes(StandardCharsets.UTF_8));
            }

            StringBuilder response = new StringBuilder();
            try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
            }

            System.out.println("응답 본문 = " + response.toString());

            JsonNode root = objectMapper.readTree(response.toString());

            StockDetailDto stockDetailDto = new StockDetailDto();
            stockDetailDto.setId(id.intValue());
            stockDetailDto.setStockCode(stockCode);
            stockDetailDto.setStockName(root.path("stk_nm").asText());
            stockDetailDto.setStockPrice(root.path("cur_prc").asText());
            stockDetailDto.setStockPredictedPrice(root.path("pred_pre").asText());
            stockDetailDto.setStockChangeRate(root.path("flu_rt").asText());
            stockDetailDto.setStockYearHigh(root.path("oyr_hgst").asText());
            stockDetailDto.setStockYearLow(root.path("oyr_lwst").asText());
            stockDetailDto.setStockFaceValue(root.path("fav").asText());
            stockDetailDto.setStockMarketCap(root.path("mac").asText());
            stockDetailDto.setStockSalesAmount(root.path("cup_nga").asText());
            stockDetailDto.setStockPer(root.path("per").asText());

            return stockDetailDto;

        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }


    public String createUrl(String endpoint) {
        String host = "https://mockapi.kiwoom.com";
        return host + endpoint;
    }


    public String createJsonData() throws JsonProcessingException {
        Map<String, String> payload = new HashMap<>();
        payload.put("grant_type", "client_credentials");
        payload.put("appkey", apiKey);
        payload.put("secretkey", apiSecret);
        return objectMapper.writeValueAsString(payload);
    }
}
