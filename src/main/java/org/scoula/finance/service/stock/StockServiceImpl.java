package org.scoula.finance.service.stock;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.finance.dto.stock.*;
import org.scoula.finance.mapper.StockMapper;
import org.scoula.finance.util.PythonExecutorUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;


import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StockMapper stockMapper;

    @Value("${stock.api-key}")
    private String stockApiKey;
    @Value("${stock.api-secret}")
    private String stockApiSecret;

    @Override
    public StockAccessTokenDto issueAndSaveToken(Long id) {
        try {
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
            try (OutputStream os = connection.getOutputStream()) {
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
                    new TypeReference<>() {
                    }
            );

            String accessToken = responseMap.get("token");
            String expiresDt = responseMap.get("expires_dt");

            StockAccessTokenDto stockAccessTokenDto = new StockAccessTokenDto(id, "8106-9967", accessToken, expiresDt);
            stockMapper.saveOrUpdateToken(stockAccessTokenDto);

            return stockAccessTokenDto;

        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    //   계좌번호, 수익률 전달
    @Override
    public StockAccountDto getAccountReturnRate(Long userId) {
        String userAccount = stockMapper.getUserAccount(userId);
        String token = stockMapper.getUserToken(userId);
        String jsonData = "{\"qry_tp\" : \"1\",\"dmst_stex_tp\" : \"KRX\"}";

        try {
            String response = sendPostRequest("/api/dostk/acnt", token, jsonData, "kt00018");

            JsonNode root = objectMapper.readTree(response);
            String totalReturnRateStr = root.path("tot_prft_rt").asText();
            BigDecimal normalizedRate = new BigDecimal(totalReturnRateStr);
            return new StockAccountDto(userId, userAccount, normalizedRate.toPlainString());

        } catch (Exception e) {
            log.error("getAccountReturnRate error: {}", e.getMessage());
            return null;
        }
    }

    //주식 리스트 조회
    @Override
    public List<StockListDto> getStockList(Long userId, StockFilterDto filterDto) {
        List<StockListDataDto> basicList = stockMapper.getStockList(filterDto);
        List<StockListDto> finalList = new ArrayList<>();
        String token = stockMapper.getUserToken(userId);

        for (StockListDataDto data : basicList) {
            String stockCode = data.getStockCode();

            try {
                // 1. API 요청 및 응답 파싱
                String response = sendPostRequest("/api/dostk/stkinfo", token,
                        String.format("{\"stk_cd\" : \"%s\"}", stockCode), "ka10001");

                JsonNode root = objectMapper.readTree(response);

                // 2. DTO 생성 및 값 설정
                StockListDto dto = new StockListDto();
                dto.setStockCode(stockCode);
                dto.setStockName(data.getStockName());
                dto.setStockReturnsData(data.getStockReturnsData());
                dto.setStockMarketType(data.getStockMarketType());
                dto.setStockSummary(data.getStockSummary());
                dto.setStockPredictedPrice(root.path("pred_pre").asText());
                dto.setStockChangeRate(root.path("flu_rt").asText());

                // 가격 부호 제거
                String curPriceRaw = root.path("cur_prc").asText();
                String curPrice = curPriceRaw.replaceAll("[^0-9]", "");
                int currentPrice = Integer.parseInt(curPrice);
                dto.setStockPrice(currentPrice);

                finalList.add(dto);

            } catch (Exception e) {
                log.warn("stock parsing failed for code {}: {}", stockCode, e.getMessage());
            }
        }

        return finalList;
    }

    // 차트 데이터 DB에 저장
    @Override
    public void updateChartData() {

        try{
            ClassPathResource resource = new ClassPathResource("python/stock/getData.py");
            File pythonFile = resource.getFile();
            String path = pythonFile.getAbsolutePath();

            // Python 실행
            PythonExecutorUtil.runPythonScript(path);

            // json 파일 DB에 저장
            String filePath = "./data/stock/output/returns_data.json";

            Map<String, Map<String,Integer>> chartMap = objectMapper.readValue(
                    new File(filePath),
                    new TypeReference<>() {
                    }
            );

            for (Map.Entry<String, Map<String, Integer>> entry : chartMap.entrySet()) {
                String stockCode = entry.getKey();
                String stockReturnsData = objectMapper.writeValueAsString(entry.getValue());

                stockMapper.updateStockReturnsData(stockCode, stockReturnsData);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // 팩터 계산 및 DB 저장
    @Override
    public void updateFactor(String analyzeDate, String resultDate, String startDate){
        try{
            ClassPathResource resource = new ClassPathResource("python/stock/getFactor.py");
            File pythonFile = resource.getFile();
            String path = pythonFile.getAbsolutePath();

            // Python 실행
            PythonExecutorUtil.runPythonScript(path, analyzeDate, resultDate, startDate);

            // json 읽기
            String filePath = "./data/stock/output/factor_result.json";
            StockFactorDto factorDto = objectMapper.readValue(
                    new File(filePath),
                    new TypeReference<>() {
                    }
            );
            
            // DB 저장
            stockMapper.insertStockFactorData(factorDto);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // 주식 상세 정보 조회
    @Override
    public StockDetailDto getStockDetail(Long id, String stockCode) {
        String token = stockMapper.getUserToken(id);

        try {
            String response = sendPostRequest("/api/dostk/stkinfo", token,
                    String.format("{\"stk_cd\" : \"%s\"}", stockCode), "ka10001");

            StockListDataDto listDto = stockMapper.getStockListDataByStockCode(stockCode);

            JsonNode root = objectMapper.readTree(response);
            StockDetailDto dto = new StockDetailDto();
            dto.setId(id.intValue());
            dto.setStockCode(stockCode);
            dto.setStockName(root.path("stk_nm").asText());
            dto.setStockPredictedPrice(root.path("pred_pre").asText());
            dto.setStockChangeRate(root.path("flu_rt").asText());
            dto.setStockChartData(listDto.getStockReturnsData());
            dto.setStockYearHigh(root.path("oyr_hgst").asText());
            dto.setStockYearLow(root.path("oyr_lwst").asText());
            dto.setStockFaceValue(root.path("fav").asText());
            dto.setStockMarketCap(root.path("mac").asText());
            dto.setStockSalesAmount(root.path("cup_nga").asText());
            dto.setStockPer(root.path("per").asText());

            // 가격 부호 제거
            String curPriceRaw = root.path("cur_prc").asText();
            String curPrice = curPriceRaw.replaceAll("[^0-9]", "");
            dto.setStockPrice(curPrice);

            return dto;

        } catch (Exception e) {
            log.error("getStockDetail error: {}", e.getMessage());
            return null;
        }
    }

    //주식 추천 로직
    @Override
    public List<StockListDto> getStockRecommendationList(Long userId, int limit, Integer amount) {
        List<StockFactorDto> factorDto = stockMapper.getStockFactorData();
        List<Map<String,Object>> stockCodeList = stockMapper.getStockCodeList();
        List<StockListDto> recommendedStocks = new ArrayList<>();
        String token = stockMapper.getUserToken(userId);
        try{
            objectMapper.writeValue(new File("factor_input.json"), factorDto);
            objectMapper.writeValue(new File("stock_code_list.json"), stockCodeList);

            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(factorDto));
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(stockCodeList));


            ClassPathResource resource = new ClassPathResource("python/stock/calcStock.py");
            File pythonFile = resource.getFile();
            String path = pythonFile.getAbsolutePath();

            PythonExecutorUtil.runPythonScript(path, "factor_input.json", "stock_code_list.json");

            File resultFile = new File("./data/stock/output/calc_result.json");

            Map<String, Double> resultMap = objectMapper.readValue(
                    resultFile,
                    new TypeReference<>() {
                    }
            );

            int count = 0;
            for(Map.Entry<String, Double> entry : resultMap.entrySet()){
                if(count >= limit) break;

                String stockCode = entry.getKey();

                String response = sendPostRequest("/api/dostk/stkinfo", token,
                        String.format("{\"stk_cd\" : \"%s\"}", stockCode), "ka10001");

                JsonNode root = objectMapper.readTree(response);

                StockListDataDto dto = stockMapper.getStockListDataByStockCode(stockCode);

                StockListDto listDto = new StockListDto();

                listDto.setStockCode(stockCode);
                listDto.setStockName(dto.getStockName());
                listDto.setStockReturnsData(dto.getStockReturnsData());
                listDto.setStockMarketType(dto.getStockMarketType());
                listDto.setStockPredictedPrice(root.path("pred_pre").asText());
                listDto.setStockChangeRate(root.path("flu_rt").asText());
                listDto.setStockSummary(dto.getStockSummary());

                String curPriceRaw = root.path("cur_prc").asText();
                String curPrice = curPriceRaw.replaceAll("[^0-9]", "");
                int currentPrice = Integer.parseInt(curPrice);

                if(amount == null){
                    listDto.setStockPrice(currentPrice);
                    recommendedStocks.add(listDto);
                    count++;
                }
                else if(currentPrice <= amount){
                    listDto.setStockPrice(currentPrice);
                    recommendedStocks.add(listDto);
                    count++;
                }
            }

        }catch (Exception e){
            System.out.println("오류 발생 " + e.getMessage());
        }
        return recommendedStocks;
    }

    public String createUrl(String endpoint) {
        String host = "https://mockapi.kiwoom.com";
        return host + endpoint;
    }

    public String createJsonData() {
        Map<String, String> payload = new HashMap<>();
        payload.put("grant_type", "client_credentials");
        payload.put("appkey", stockApiKey);
        payload.put("secretkey", stockApiSecret);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("createJsonData error: {}", e.getMessage());
            return "{}";
        }
    }

    // api 요청 함수
    private String sendPostRequest(String endpoint, String token, String jsonData, String apiId) {
        int maxRetries = 3;
        int retryDelayMs = 500;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                URL url = new URL(createUrl(endpoint));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                connection.setRequestProperty("authorization", "Bearer " + token);
                connection.setRequestProperty("api-id", apiId);
                connection.setDoOutput(true);

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonData.getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = connection.getResponseCode();

                // 429: Too Many Requests
                if (responseCode == 429) {
                    log.warn("Received 429 Too Many Requests. Attempt {}/{}", attempt, maxRetries);
                    Thread.sleep(retryDelayMs); // 일정 시간 대기 후 재시도
                    continue;
                }

                InputStream inputStream = (responseCode >= 200 && responseCode < 300)
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                StringBuilder response = new StringBuilder();
                try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
                    while (scanner.hasNextLine()) {
                        response.append(scanner.nextLine());
                    }
                }

                return response.toString();

            } catch (IOException | InterruptedException e) {
                log.error("sendPostRequest error (attempt {}): {}", attempt, e.getMessage());
            }
        }

        log.error("sendPostRequest failed after {} attempts", maxRetries);
        return "";
    }
}