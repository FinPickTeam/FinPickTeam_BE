package org.scoula.finance.service.stock;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.finance.dto.deposit.DepositRecommendationDto;
import org.scoula.finance.dto.stock.*;
import org.scoula.finance.mapper.StockMapper;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StockMapper stockMapper;

    @Value("${openai.api-key}")
    private String openaiApiKey;
    @Value("${openai.api-url}")
    private String openaiApiUrl;

    @Value("${stock.api-key}")
    private String stockApiKey;
    @Value("${stock.api-secret}")
    private String stockApiSecret;

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
public StockAccountDto getAccountReturnRate(Long id) {
    String userAccount = stockMapper.getUserAccount(id);
    String token = stockMapper.getUserToken(id);
    String jsonData = "{\"qry_tp\" : \"1\",\"dmst_stex_tp\" : \"KRX\"}";

    try {
        String response = sendPostRequest("/api/dostk/acnt", token, jsonData, "kt00018");

        JsonNode root = objectMapper.readTree(response);
        String totalReturnRateStr = root.path("tot_prft_rt").asText();
        BigDecimal normalizedRate = new BigDecimal(totalReturnRateStr);
        return new StockAccountDto(userAccount, normalizedRate.toPlainString());

    } catch (Exception e) {
        log.error("getAccountReturnRate error: {}", e.getMessage());
        return null;
    }
}

    //주식 리스트 조회
    @Override
    public List<StockListDto> getStockList(Long userId, String market, String sortName, String sortPrice) {
        List<StockListDataDto> basicList = stockMapper.getStockList();
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
                dto.setStockMarketType(data.getStockMarketType());
                dto.setStockSummary(data.getStockSummary());
                dto.setStockPredictedPrice(root.path("pred_pre").asText());
                dto.setStockChangeRate(root.path("flu_rt").asText());
                // 가격 부호 제거
                String curPriceRaw = root.path("cur_prc").asText();
                String curPrice = curPriceRaw.replace("[^0-9]", "");
                dto.setStockPrice(curPrice);

                // 3. 차트 데이터 호출
                String chartJson = stockMapper.getChartCache(stockCode);
                dto.setStockChartData(chartJson);

                finalList.add(dto);

            } catch (Exception e) {
                log.warn("stock parsing failed for code {}: {}", stockCode, e.getMessage());
            }
        }
        //  마켓 필터 (KOSPI or KOSDAQ)
        if (market != null && !market.isBlank()) {
            finalList = finalList.stream()
                    .filter(dto -> market.equalsIgnoreCase(dto.getStockMarketType()))
                    .collect(Collectors.toList());
        }

        // 이름 정렬
        if ("asc".equalsIgnoreCase(sortName)) {
            finalList.sort(Comparator.comparing(StockListDto::getStockName));
        } else if ("desc".equalsIgnoreCase(sortName)) {
            finalList.sort(Comparator.comparing(StockListDto::getStockName).reversed());
        }

        // 가격 정렬
        if ("asc".equalsIgnoreCase(sortPrice)) {
            finalList.sort(Comparator.comparingInt(dto -> Integer.parseInt(dto.getStockPrice())));
        } else if ("desc".equalsIgnoreCase(sortPrice)) {
            finalList.sort(Comparator.comparingInt((StockListDto dto) -> Integer.parseInt(dto.getStockPrice())).reversed());
        }

        return finalList;
    }

    // 차트 데이터 DB에 저장
    @Override
    public void fetchAndCacheChartData(Long id) {
        String token = stockMapper.getUserToken(id);
        List<String> stockList = stockMapper.getStockCodeList();

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String baseDate = today.format(formatter);

        for (String stockCode : stockList) {
            try {
                String response = sendPostRequest("/api/dostk/chart", token,
                        String.format("{\"stk_cd\" : \"%s\", \"base_dt\" : \"%s\", \"upd_stkpc_tp\" : \"1\"}",
                                stockCode, baseDate), "ka10081");

                JsonNode root = objectMapper.readTree(response);
                JsonNode outputArray = root.path("stk_dt_pole_chart_qry");

                List<StockPricePointDto> priceList = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    JsonNode dayData = outputArray.get(i);
                    String date = dayData.path("dt").asText();
                    String price = dayData.path("cur_prc").asText();

                    priceList.add(new StockPricePointDto(date, price));
                }

                String jsonData = objectMapper.writeValueAsString(priceList);
                StockChartDataDto cacheDto = new StockChartDataDto(stockCode, jsonData, baseDate);
                stockMapper.saveChartCache(cacheDto);

                Thread.sleep(210); // 1초에 5건 제한 대응

            } catch (Exception e) {
                log.warn("차트 데이터 저장 실패 - stockCode {}: {}", stockCode, e.getMessage());
            }
        }
    }

    // 주식 상세 정보 조회
    @Override
    public StockDetailDto getStockDetail(Long id, String stockCode) {
        String token = stockMapper.getUserToken(id);
        String jsonData = String.format("{\"stk_cd\" : \"%s\"}", stockCode);

        try {
            String response = sendPostRequest("/api/dostk/stkinfo", token, jsonData, "ka10001");

            JsonNode root = objectMapper.readTree(response);
            StockDetailDto dto = new StockDetailDto();
            dto.setId(id.intValue());
            dto.setStockCode(stockCode);
            dto.setStockName(root.path("stk_nm").asText());
            dto.setStockPredictedPrice(root.path("pred_pre").asText());
            dto.setStockChangeRate(root.path("flu_rt").asText());
            dto.setStockChartData(stockMapper.getChartCache(stockCode));
            dto.setStockYearHigh(root.path("oyr_hgst").asText());
            dto.setStockYearLow(root.path("oyr_lwst").asText());
            dto.setStockFaceValue(root.path("fav").asText());
            dto.setStockMarketCap(root.path("mac").asText());
            dto.setStockSalesAmount(root.path("cup_nga").asText());
            dto.setStockPer(root.path("per").asText());

            // 가격 부호 제거
            String curPriceRaw = root.path("cur_prc").asText();
            String curPrice = curPriceRaw.replace("[^0-9]", "");
            dto.setStockPrice(curPrice);

            return dto;

        } catch (Exception e) {
            log.error("getStockDetail error: {}", e.getMessage());
            return null;
        }
    }

    //주식 추천 로직
    @Override
    public List<StockListDto> getStockRecommendationList(Long id, int limit) {
        List<String> stockList = stockMapper.getStockCodeList();
        String prompt = generatePrompt(stockList);

        try {
            String gptResponse = callOpenAiApi(prompt).trim();

            if (gptResponse.startsWith("```")) {
                gptResponse = gptResponse.replaceFirst("^```json\\s*", "")
                        .replaceFirst("\\s*```$", "");
            }

            JsonNode jsonArray = objectMapper.readTree(gptResponse);
            Map<String, String> gptRecommendationMap = new HashMap<>(); // GPT 추천결과를 Map<stockCode, reason>으로 저장

            for (JsonNode node : jsonArray) {
                String stockCode = node.path("stockCode").asText().trim();
                String reason = node.path("reason").asText().trim();
                gptRecommendationMap.put(stockCode, reason);
            }

            // DB에서 추천 종목의 상세 정보 가져오기
            List<StockListDataDto> baseDataList = stockMapper.getStockList();  // 모든 주식 정보
            Map<String, StockListDataDto> baseDataMap = baseDataList.stream()
                    .collect(Collectors.toMap(StockListDataDto::getStockCode, dto -> dto));

            List<StockListDto> finalList = new ArrayList<>();

            for (String code : gptRecommendationMap.keySet()) {
                StockListDataDto data = baseDataMap.get(code);
                if (data == null) continue;

                StockDetailDto detail = getStockDetail(id, code);
                if (detail == null) continue;

                StockListDto dto = new StockListDto();
                dto.setStockCode(code);
                dto.setStockName(data.getStockName());
                dto.setStockChartData(stockMapper.getChartCache(code));
                dto.setStockPrice(detail.getStockPrice());
                dto.setStockMarketType(data.getStockMarketType());
                dto.setStockPredictedPrice(detail.getStockPredictedPrice());
                dto.setStockChangeRate(detail.getStockChangeRate());
                dto.setStockSummary(data.getStockSummary());
                dto.setStockGptReason(gptRecommendationMap.get(code));

                finalList.add(dto);
            }
            return finalList;
        } catch (Exception e) {
            log.error("GPT 응답 처리 중 오류 발생", e);
            return Collections.emptyList();  // 예외 시 빈 리스트 반환
        }
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

    // GPT 프롬프트
    public String generatePrompt(List<String> stockCode){
        if(stockCode.isEmpty()){
            return "데이터 없음";
        }

        //나중에 사용자 데이터 추가 필요
        StringBuilder sb = new StringBuilder();
        sb.append("사용자가 ").append(stockCode).append("의 주식중에서 하나를 투자하려고 해")
                .append("이 주식중에서 5개를 선택해주고 ")
                .append("이 상품을 왜 추천하는지, 너무 딱딱하지 않고 ")
                .append("일상적인 말투로 설명해 줘. 사용자 입장에서 공감되도록, ")
                .append("편하게 말하듯 써줘. 예를 들면 '최근 거래량이 많이 늘었어요!' 같은 말투. ")
                .append("결과는 아래 JSON 형식으로만:\n")
                .append("[\n")
                .append("  { \"stockCode\": \"주식코드1\", \"reason\": \"추천 이유1\" },\n")
                .append("  { \"stockCode\": \"주식코드2\", \"reason\": \"추천 이유2\" }\n")
                .append("]\n")
                .append("다른 말은 하지 말고 JSON 배열만 반환해.");

        return sb.toString();
    }

    private String callOpenAiApi(String prompt) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        String requestBody = objectMapper.writeValueAsString(
                Map.of(
                        "model", "gpt-4o",
                        "messages", List.of(
                                Map.of("role", "system", "content", "너는 금융상품 전문가야."),
                                Map.of("role", "user", "content", prompt)
                        ),
                        "temperature", 0.7
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openaiApiKey);
        headers.set("Content-Type", "application/json; charset=UTF-8");

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(openaiApiUrl, HttpMethod.POST, request, String.class);

        return objectMapper.readTree(response.getBody()).at("/choices/0/message/content").asText();
    }
}
