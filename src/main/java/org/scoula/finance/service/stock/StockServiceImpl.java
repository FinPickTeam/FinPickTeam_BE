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
import org.scoula.finance.util.PythonExecutorUtil.JobWorkspace;
import org.scoula.survey.domain.SurveyVO;
import org.scoula.survey.mapper.SurveyMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonData = createJsonData();

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
            Map<String, String> responseMap = objectMapper.readValue(response.toString(), new TypeReference<>() {});
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

    @Override
    public List<StockListDto> getStockList(Long userId, StockFilterDto filterDto) {
        List<StockListDataDto> basicList = stockMapper.getStockList(filterDto);
        List<StockListDto> finalList = new ArrayList<>();
        String token = stockMapper.getUserToken(userId);

        for (StockListDataDto data : basicList) {
            String stockCode = data.getStockCode();
            try {
                String response = KiwoomApiUtils.sendPostRequest("/api/dostk/stkinfo", token,
                        String.format("{\"stk_cd\" : \"%s\"}", stockCode), "ka10001");

                JsonNode root = objectMapper.readTree(response);

                StockListDto dto = new StockListDto();
                dto.setStockCode(stockCode);
                dto.setStockName(data.getStockName());
                dto.setStockReturnsData(data.getStockReturnsData());
                dto.setStockMarketType(data.getStockMarketType());
                dto.setStockSummary(data.getStockSummary());
                dto.setStockPredictedPrice(root.path("pred_pre").asText());
                dto.setStockChangeRate(root.path("flu_rt").asText());

                String curPriceRaw = root.path("cur_prc").asText();
                String curPrice = curPriceRaw.replaceAll("[^0-9]", "");
                dto.setStockPrice(Integer.parseInt(curPrice));

                finalList.add(dto);

            } catch (Exception e) {
                log.warn("stock parsing failed for code {}: {}", stockCode, e.getMessage());
            }
        }
        return finalList;
    }

    // ---------- 파이썬: 차트 데이터 ----------
    @Override
    public void updateChartData() {
        ClassPathResource res = new ClassPathResource("python/stock/getData.py");
        JobWorkspace ws = null;
        try{
            File pyRoot = PythonExecutorUtil.getPyRootFrom(res);
            ws = PythonExecutorUtil.createJobWorkspace(pyRoot);

            File scriptFile = PythonExecutorUtil.asFileOrTemp(res);
            PythonExecutorUtil.runPythonScript(scriptFile.getAbsolutePath(), ws.root);

            File file = ws.resolve("data/stock/output/returns_data.json");
            Map<String, Map<String,Integer>> chartMap = objectMapper.readValue(file, new TypeReference<>() {});
            for (Map.Entry<String, Map<String, Integer>> entry : chartMap.entrySet()) {
                String stockCode = entry.getKey();
                String stockReturnsData = objectMapper.writeValueAsString(entry.getValue());
                stockMapper.updateStockReturnsData(stockCode, stockReturnsData);
            }
        } catch (Exception e){
            log.error("updateChartData error", e);
        } finally {
            if (ws != null) ws.cleanupQuietly();
        }
    }

    // ---------- 파이썬: 팩터 ----------
    @Override
    public void updateFactor(String analyzeDate, String resultDate, String startDate){
        ClassPathResource res = new ClassPathResource("python/stock/getFactor.py");
        JobWorkspace ws = null;
        try{
            File pyRoot = PythonExecutorUtil.getPyRootFrom(res);
            ws = PythonExecutorUtil.createJobWorkspace(pyRoot);

            File scriptFile = PythonExecutorUtil.asFileOrTemp(res);
            PythonExecutorUtil.runPythonScript(scriptFile.getAbsolutePath(), ws.root, analyzeDate, resultDate, startDate);

            File file = ws.resolve("data/stock/output/factor_result.json");
            StockFactorDto factorDto = objectMapper.readValue(file, new TypeReference<>() {});
            stockMapper.insertStockFactorData(factorDto);

        } catch (Exception e){
            log.error("updateFactor error", e);
        } finally {
            if (ws != null) ws.cleanupQuietly();
        }
    }

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

            String curPriceRaw = root.path("cur_prc").asText();
            String curPrice = curPriceRaw.replaceAll("[^0-9]", "");
            dto.setStockPrice(curPrice);
            return dto;

        } catch (Exception e) {
            log.error("getStockDetail error: {}", e.getMessage());
            return null;
        }
    }

    // ---------- 파이썬: 개별 수익률 ----------
    @Override
    public String getStockReturn(String stockCode, String startDate, String endDate){
        ClassPathResource res = new ClassPathResource("python/stock/getStockReturn.py");
        JobWorkspace ws = null;
        try{
            File pyRoot = PythonExecutorUtil.getPyRootFrom(res);
            ws = PythonExecutorUtil.createJobWorkspace(pyRoot);

            File scriptFile = PythonExecutorUtil.asFileOrTemp(res);
            PythonExecutorUtil.runPythonScript(scriptFile.getAbsolutePath(), ws.root, stockCode, startDate, endDate);

            File jsonFile = ws.resolve("data/stock/output/stock_return.json");
            if (!jsonFile.exists()) return "0";

            JsonNode root = objectMapper.readTree(jsonFile);
            if (root.hasNonNull("pnl")) return root.get("pnl").asText();
            return "0";

        } catch (Exception e){
            log.error("getStockReturn error", e);
            return stockCode;
        } finally {
            if (ws != null) ws.cleanupQuietly();
        }
    }

    // ---------- 파이썬: 추천 ----------
    @Override
    public List<StockListDto> getStockRecommendationList(Long userId, int limit, Integer amount) {
        List<StockFactorDto> factorDto = stockMapper.getStockFactorData();
        List<Map<String,Object>> stockCodeList = stockMapper.getStockCodeList();
        List<StockListDto> recommendedStocks = new ArrayList<>();
        List<String> gptRecommendedStocks = new ArrayList<>();
        String token = stockMapper.getUserToken(userId);

        ClassPathResource res = new ClassPathResource("python/stock/calcStock.py");
        JobWorkspace ws = null;

        try{
            File pyRoot = PythonExecutorUtil.getPyRootFrom(res);
            ws = PythonExecutorUtil.createJobWorkspace(pyRoot);

            // 입력 파일 (요청 폴더 루트)
            File factorInput = ws.resolve("factor_input.json");
            File codeListInput = ws.resolve("stock_code_list.json");
            objectMapper.writeValue(factorInput, factorDto);
            objectMapper.writeValue(codeListInput, stockCodeList);

            // 실행 (상대파일명 인자로 전달)
            File scriptFile = PythonExecutorUtil.asFileOrTemp(res);
            PythonExecutorUtil.runPythonScript(
                    scriptFile.getAbsolutePath(),
                    ws.root,
                    "factor_input.json",
                    "stock_code_list.json"
            );

            // 결과 읽기
            File resultFile = ws.resolve("data/stock/output/calc_result.json");
            Map<String, Double> resultMap = objectMapper.readValue(resultFile, new TypeReference<>() {});
            List<String> stockCodeListForGpt = new ArrayList<>(resultMap.keySet());

            SurveyVO surveyVo = surveyMapper.selectById(userId);
            String prompt = generatePrompt(stockCodeListForGpt, surveyVo);

            try {
                String gptResponse = callOpenAiApi(prompt);
                if (gptResponse == null) throw new IllegalStateException("GPT 응답 없음");

                String content = gptResponse.trim()
                        .replaceAll("(?s)^```(?:json)?\\s*", "")
                        .replaceAll("(?s)\\s*```$", "");

                int s = content.indexOf('[');
                int e = content.lastIndexOf(']');
                if (s >= 0 && e > s) content = content.substring(s, e + 1);

                JsonNode root = objectMapper.readTree(content);
                if (!root.isArray()) throw new IllegalArgumentException("GPT 응답이 JSON 배열이 아님: " + content);

                LinkedHashSet<String> dedup = new LinkedHashSet<>();
                for (JsonNode node : root) {
                    String sc = node.hasNonNull("stockCode")
                            ? node.get("stockCode").asText("")
                            : (node.hasNonNull("stock_code") ? node.get("stock_code").asText("") : "");
                    sc = sc == null ? "" : sc.trim();
                    if (!sc.isEmpty()) dedup.add(sc);
                }
                gptRecommendedStocks.addAll(dedup);

            } catch (Exception e) {
                log.warn("OpenAI parsing warn", e);
            }

            int count = 0;
            for (String sc : gptRecommendedStocks){
                if (count >= limit) break;

                String response = KiwoomApiUtils.sendPostRequest("/api/dostk/stkinfo", token,
                        String.format("{\"stk_cd\" : \"%s\"}", sc), "ka10001");

                JsonNode root = objectMapper.readTree(response);
                StockListDataDto dto = stockMapper.getStockListDataByStockCode(sc);

                StockListDto listDto = new StockListDto();
                listDto.setStockCode(sc);
                listDto.setStockName(dto.getStockName());
                listDto.setStockReturnsData(dto.getStockReturnsData());
                listDto.setStockMarketType(dto.getStockMarketType());
                listDto.setStockPredictedPrice(root.path("pred_pre").asText());
                listDto.setStockChangeRate(root.path("flu_rt").asText());
                listDto.setStockSummary(dto.getStockSummary());

                String curPriceRaw = root.path("cur_prc").asText();
                String curPrice = curPriceRaw.replaceAll("[^0-9]", "");
                int currentPrice = Integer.parseInt(curPrice);

                if (amount == null || currentPrice <= amount) {
                    listDto.setStockPrice(currentPrice);
                    recommendedStocks.add(listDto);
                    count++;
                }
            }

        } catch (Exception e){
            log.error("getStockRecommendationList error", e);
        } finally {
            if (ws != null) ws.cleanupQuietly();
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

    public String generatePrompt(List<String> stockCode, SurveyVO surveyVo) {
        if(stockCode.isEmpty()){
            return "데이터 없음";
        }
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
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

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
