package org.scoula.finance.service.stock;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.finance.dto.stock.*;
import org.scoula.finance.mapper.StockMapper;
import org.scoula.finance.util.KiwoomApiUtils;
import org.scoula.finance.util.PythonExecutorUtil;
import org.scoula.survey.domain.SurveyVO;
import org.scoula.survey.mapper.SurveyMapper;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StockMapper stockMapper;
    private final SurveyMapper surveyMapper;

    @Value("${stock.api.key}")
    private String stockApiKey;
    @Value("${stock.api.secret}")
    private String stockApiSecret;

    @Value("${openai.api.key}")
    private String openaiApiKey;
    @Value("${openai.api.url}")
    private String openaiApiUrl;

    @Override
    public StockAccessTokenDto issueAndSaveToken(Long id) {
        try {
            String endpoint = "/oauth2/token";

            URL url = new URL(KiwoomApiUtils.createUrl(endpoint));
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
            String response = KiwoomApiUtils.sendPostRequest("/api/dostk/acnt", token, jsonData, "kt00018");

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
                String response = KiwoomApiUtils.sendPostRequest("/api/dostk/stkinfo", token,
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
            String response = KiwoomApiUtils.sendPostRequest("/api/dostk/stkinfo", token,
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
        List<String> gptRecommendedStocks = new ArrayList<>();
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

            System.out.println(resultFile);

            Map<String, Double> resultMap = objectMapper.readValue(
                    resultFile,
                    new TypeReference<>() {
                    }
            );

            List<String> stockCodeListForGpt = new ArrayList<>(resultMap.keySet());
            SurveyVO surveyVo = surveyMapper.selectById(userId);

            String prompt = generatePrompt(stockCodeListForGpt, surveyVo);

            try {
                String gptResponse = callOpenAiApi(prompt);
                if (gptResponse == null) throw new IllegalStateException("GPT 응답 없음");

                String content = gptResponse.trim();

                content = content.replaceAll("(?s)^```(?:json)?\\s*", "")
                        .replaceAll("(?s)\\s*```$", "");

                System.out.println("gpt 응답 : " + content);

                int s = content.indexOf('[');
                int e = content.lastIndexOf(']');
                if (s >= 0 && e > s) {
                    content = content.substring(s, e + 1);
                }

                JsonNode root = objectMapper.readTree(content);
                if (!root.isArray()) {
                    throw new IllegalArgumentException("GPT 응답이 JSON 배열이 아님: " + content);
                }

                LinkedHashSet<String> dedup = new LinkedHashSet<>();
                for (JsonNode node : root) {
                    String stockCode = null;
                    if (node.hasNonNull("stockCode")) {
                        stockCode = node.get("stockCode").asText("");
                    } else if (node.hasNonNull("stock_code")) {
                        stockCode = node.get("stock_code").asText("");
                    }
                    if (stockCode != null) {
                        stockCode = stockCode.trim();
                        if (!stockCode.isEmpty()) {
                            dedup.add(stockCode);
                        }
                    }
                }
                gptRecommendedStocks.addAll(dedup);
            } catch (Exception e) {
                e.printStackTrace(); // 필요시 로거로 변경
            }

            int count = 0;
            for(String stockCode : gptRecommendedStocks){
                if(count >= limit) break;

                String response = KiwoomApiUtils.sendPostRequest("/api/dostk/stkinfo", token,
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

    // GPT 프롬프트
    public String generatePrompt(List<String> stockCode, SurveyVO surveyVo) {
        if(stockCode.isEmpty()){
            return "데이터 없음";
        }
        
        // 추후 수정 필요
        StringBuilder sb = new StringBuilder();
        sb.append("너는 금융 전문가이며, 4요인(4-Factor) 모델을 기반으로 주식 추천을 해주는 역할이야. ")
                .append("아래는 이미 4요인 모델로 알파(α)를 계산하고, 정보 비율(IR) 기준으로 순위화된 주식 목록이야. ")
                .append("사용자의 투자 성향 답변을 참고해서, 목록 중에서 정확히 5개 주식을 추천해. ")
                .append("선정 시 다음 기준을 따르도록 해: ")
                .append("1. 사용자의 위험 선호도와 투자 목적에 맞춰 변동성이 높은/낮은 종목을 선택해. ")
                .append("2. 투자 경험이 적으면 안정적인 종목 비중을 높이고, 경험이 많으면 성장성 높은 종목을 포함해. ")
                .append("3. 투자 금액이 적으면 분산투자 효과를 높이고, 금액이 크면 고수익 가능 종목을 일부 포함해. ")
                .append("4. 목록의 순위가 높을수록 우선 추천하되, 투자 성향과 맞지 않으면 제외 가능. ")
                .append("5. 동일 산업군 종목이 너무 많지 않도록 다양하게 구성해. ")
                .append("5) 출력은 JSON 배열만. 설명·주석·코드펜스 금지. 키는 stockCode만 사용.")
                .append(surveyVo.getQuestion1()).append("\n")
                .append("2번째 투자 성향 질문은 [금융투자상품 취득 및 처분 목적]이고 사용자의 답변은 ")
                .append(surveyVo.getQuestion2()).append("\n")
                .append("3번째 투자 성향 질문은 [투자수익 및 위험에 대한 태도]이고 사용자의 답변은 ")
                .append(surveyVo.getQuestion3()).append("\n")
                .append("4번째 투자 성향 질문은 [투자경험]이고 사용자의 답변은 ")
                .append(surveyVo.getQuestion4()).append("\n")
                .append("5번째 투자 성향 질문은 [한달 동안의 투자 금액]이고 사용자의 답변은 ")
                .append(surveyVo.getQuestion5()).append("\n")
                .append("[주식 목록]\n")
                .append(stockCode).append("\n\n")
                .append("\n출력 예시(정확히 이 형식):\n")
                .append("[\n")
                .append("  { \"stockCode\": \"005930\" },\n")
                .append("  { \"stockCode\": \"000660\" },\n")
                .append("  { \"stockCode\": \"035420\" },\n")
                .append("  { \"stockCode\": \"068270\" },\n")
                .append("  { \"stockCode\": \"051910\" }\n")
                .append("]\n")
                .append("다른 말은 절대 하지 말고 JSON 배열만 반환해.");

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
                                Map.of("role", "system", "content", "너는 주식 투자 전문가야."),
                                Map.of("role", "user", "content", prompt)
                        ),
                        "temperature", 0.2
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