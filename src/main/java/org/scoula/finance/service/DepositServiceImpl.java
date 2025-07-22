package org.scoula.finance.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.scoula.finance.dto.DepositFilterDto;
import org.scoula.finance.dto.DepositListDto;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.scoula.finance.dto.DepositDetailDto;
import org.scoula.finance.dto.DepositRecommendationDto;
import org.scoula.finance.mapper.DepositMapper;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositServiceImpl implements DepositService {
    private final DepositMapper depositMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${openai.api-key}")
    private String apiKey;
    @Value("${openai.api-url}")
    private String apiUrl;

    //예금 전체 조회
    @Override
    public List<DepositListDto> getDeposits(DepositFilterDto filter){
        if (isEmpty(filter)) {
            // 전체 조회 기본 정렬
            return depositMapper.selectAllDeposits();
        } else {
            // 조건부 조회
            return depositMapper.selectDepositsWithFilter(filter);
        }
    }


    // 예금 상세 조회
    @Override
    public DepositDetailDto selectDepositByProductName(String depositProductName) {
        return depositMapper.selectDepositByProductName(depositProductName);
    }

    // 예금 추천
    @Override
    public List<Map<String, Object>> getAllDepositRecommendations(int amount, int period) {
        List<DepositRecommendationDto> filteredList = depositMapper.selectAllDepositRecommendations().stream()
                .filter(dto -> checkAmount(dto.getDepositSubscriptionAmount(), amount)
                        && checkPeriod(dto.getDepositContractPeriod(), period))
                .toList();

        List<Map<String, Object>> resultList = new ArrayList<>();

        if (filteredList.isEmpty()) {
            log.warn("필터링된 예금 상품이 없습니다.");
            return resultList;
        }

        // gpt 프롬프트
        String prompt = generatePrompt(filteredList, amount, period);

        // gpt 실행
        try {
            String gptResponse = callOpenAiApi(prompt).trim();

            if (gptResponse.startsWith("```")) {
                gptResponse = gptResponse.replaceFirst("^```json\\s*", "")
                        .replaceFirst("\\s*```$", "");
            }

            JsonNode jsonArray = objectMapper.readTree(gptResponse);

            // gpt가 추천한 내용 select
            for (JsonNode node : jsonArray) {
                String productName = node.path("productName").asText().trim();
                String reason = node.path("reason").asText().trim();

                List<DepositRecommendationDto> matched = depositMapper.selectDepositsByProductName(productName);

                for (DepositRecommendationDto dto : matched) {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("product", dto);
                    entry.put("reason", reason);
                    resultList.add(entry);
                }
            }

        } catch (Exception e) {
            log.error("GPT 응답 처리 중 오류 발생", e);
        }

        return resultList;
    }
// 필터 사용했는지 확인
    private boolean isEmpty(DepositFilterDto dto) {
        return dto.getBankName() == null &&
                dto.getContractPeriodMonth() == null &&
                dto.getMinSubscriptionAmount() == null &&
                dto.getRateOrder() == null;
    }

//    계약 기간 확인
    private boolean checkPeriod(String periodStr, int period){
        try{
            String raw = periodStr.replaceAll("-","");
            if(periodStr.contains("이상") && periodStr.contains("이하")){
                String[] parts= raw.split("이상|이하");
                int min = Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
                return min <= period + 3;
            }
            else if(raw.contains(",")){
                String[] parts= raw.split(",");
                int target = Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
                return target <= period + 3;
            }else{
                int target = Integer.parseInt(raw.replaceAll("[^0-9]",""));
                return target <= period + 3;
            }
        } catch (Exception e) {
            log.error("계약기간 파싱 오류: {}", periodStr, e);
        }
        return false;
    }

//  가입금액 범위 확인
    private boolean checkAmount(String amountStr, int amount){
        final int recommendMargin = Math.max((int)(amount * 0.2), 500_000); // 추천범위: 최소 50만원, 최대 사용자가 입력한 금액 * 20%

        try{
            String raw = amountStr.replaceAll("[\\s\"]", "");
            
            if(raw.contains("이상") && raw.contains("이하")){
                String[] parts = raw.split("이상|이하");
                int min = parseMoney(parts[0]);
                return amount + recommendMargin >= min;
            }
            else if(raw.contains("이상")){
                int min = parseMoney(raw.split("이상")[0]);
                return amount + recommendMargin >= min;
            }
            else if(raw.contains("이하")){
                return true;
            }
            else{
                log.warn("처리 안된 가입금액 형식: {}", amountStr);
            }
        } catch (Exception e) {
            log.error("가입금액 파싱 오류: {}", amountStr, e);
        }
        return false;
    }

    //    금액 단위 변경
    private int parseMoney(String moneyStr){
        moneyStr = moneyStr.replaceAll(",","");
        if(moneyStr.contains("억원")){
            return Integer.parseInt(moneyStr.replaceAll("[^0-9]","")) * 100_000_000;
        }
        else if(moneyStr.contains("만원")){
            return Integer.parseInt(moneyStr.replaceAll("[^0-9]","")) * 10_000;
        }
        else if(moneyStr.contains("천원")){
            return Integer.parseInt(moneyStr.replaceAll("[^0-9]","")) * 1_000;
        }
        return Integer.parseInt(moneyStr);
    }

    // GPT 프롬프트
    public String generatePrompt(List<DepositRecommendationDto> list, int amount, int period){
        if(list.isEmpty()){
            return "데이터 없음";
        }

        //나중에 사용자 데이터 추가 필요
        StringBuilder sb = new StringBuilder();
        sb.append("사용자가 ").append(amount).append("원을 ").append(period)
                .append("개월 동안 예금하려고 해. 아래 상품 각각에 대해 5개를 사용자에 맞게 추천해주고")
                .append("이 상품을 왜 추천하는지, 너무 딱딱하지 않고 ")
                .append("일상적인 말투로 설명해 줘. 사용자 입장에서 공감되도록, ")
                .append("편하게 말하듯 써줘. 예를 들면 '가입도 쉽고 이자도 괜찮아요' 같은 말투. ")
                .append("결과는 아래 JSON 형식으로만:\n")
                .append("[\n")
                .append("  { \"productName\": \"상품명1\", \"reason\": \"추천 이유1\" },\n")
                .append("  { \"productName\": \"상품명2\", \"reason\": \"추천 이유2\" }\n")
                .append("]\n")
                .append("다른 말은 하지 말고 JSON 배열만 반환해.");

        for (DepositRecommendationDto dto : list) {
            sb.append("- ")
                    .append(dto.getDepositProductName()).append(" ")
                    .append(dto.getDepositContractPeriod()).append("개월 / ")
                    .append(dto.getDepositSubscriptionAmount()).append("원 / ")
                    .append(dto.getDepositBasicRate()).append("% / ")
                    .append(dto.getDepositMaxRate()).append("% / ");
        }
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
        headers.setBearerAuth(apiKey);
        headers.set("Content-Type", "application/json; charset=UTF-8");

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);

        return objectMapper.readTree(response.getBody()).at("/choices/0/message/content").asText();
    }

}
