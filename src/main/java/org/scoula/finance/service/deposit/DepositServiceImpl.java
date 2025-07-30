package org.scoula.finance.service.deposit;

import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.scoula.finance.dto.deposit.*;
import org.scoula.finance.util.UtilityCalculator;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.scoula.finance.mapper.DepositMapper;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositServiceImpl implements DepositService {
    private final DepositMapper depositMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${python.script-path}")
    private String pythonScriptPath;

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
    public List<DepositListDto> getAllDepositRecommendations(int amount, int period, DepositUserConditionDto depositUserConditionDto) {
        List<DepositRecommendationDto> filteredList = depositMapper.selectAllDepositRecommendations().stream()
                .filter(dto -> checkAmount(dto.getDepositSubscriptionAmount(), amount)
                        && checkPeriod(dto.getDepositContractPeriod(), period))
                .toList();

        List<Map<String, Object>> resultList = new ArrayList<>();

        System.out.println("필터된 상품 개수: " + filteredList.size());
        if (filteredList.isEmpty()) {
            log.warn("필터링된 예금 상품이 없습니다.");
            return Collections.emptyList();
        }

        try {
            // 1. payload 생성
            List<ProductConditionPayload> productPayloads = filteredList.stream()
                    .map(dto -> new ProductConditionPayload(dto.getDepositProductName(), dto.getDepositPreferentialRate()))
                    .toList();
            PythonRequestPayload payload = new PythonRequestPayload(depositUserConditionDto, productPayloads);

            // 2. input.json 저장
            File inputFile = new File("data/input/input.json");
            inputFile.getParentFile().mkdirs(); // 디렉토리 없으면 생성
            objectMapper.writeValue(inputFile, payload);
            System.out.println("✅ input.json 저장 완료: " + inputFile.getAbsolutePath());

            // 3. Python 실행
            String pythonScriptPath = "C:/Users/park/Desktop/kB_final_project/FinPickTeam_BE/src/main/java/org/scoula/finance/service/deposit/python/analyze.py";
            ProcessBuilder builder = new ProcessBuilder("python",
                    pythonScriptPath
            );
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Python] " + line);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Python 실행 실패(exit code: " + exitCode + ")");
            }

            // 4. CSV 결과 파싱
            File outputFile = new File("data/output/output.csv");
            outputFile.getParentFile().mkdirs(); // 디렉토리 없으면 생성

            List<Map<String, Object>> csvResult = parseCsvResult(outputFile.getPath());
            System.out.println("=== Python CSV 결과 ===");
            csvResult.forEach(System.out::println);

            // 5. 유틸리티 계산
            UtilityCalculator calculator = new UtilityCalculator();
            for (Map<String, Object> row : csvResult) {
                String name = (String) row.get("name");
                double totalRate = (Double) row.get("totalRate");
                int matchedCount = ((Double) row.get("matchedCount")).intValue();

                Optional<DepositRecommendationDto> match = filteredList.stream()
                        .filter(d -> {
                            boolean matched = d.getDepositProductName().equals(name);
                            if (matched) {
                                log.debug("매칭 성공: {}", d.getDepositProductName());
                            } else {
                                log.debug("불일치: {} != {}", d.getDepositProductName(), name);
                            }
                            return matched;
                        })
                        .findFirst();

                if (match.isPresent()) {
                    DepositRecommendationDto dto = match.get();
                    double utility = calculator.calculateUtility(
                            dto.getDepositBasicRate(),
                            totalRate,
                            matchedCount,
                            0.8 // 중요도 가중치 (예: 사용자 입력 기반 변경 가능)
                    );

                    Map<String, Object> result = new HashMap<>();
                    result.put("상품명", dto.getDepositProductName());
                    result.put("유틸리티", utility);
                    resultList.add(result);
                }
            }

            System.out.println("resultList.size = " + resultList.size());

            // 6. 정렬 후 최종 추천
            resultList.sort((a, b) -> Double.compare((Double) b.get("유틸리티"), (Double) a.get("유틸리티")));
            List<String> top5Names = resultList.stream()
                    .limit(5)
                    .map(r -> (String) r.get("상품명"))
                    .toList();

            return depositMapper.selectDepositListByProductName(top5Names);

        } catch (Exception e) {
            e.printStackTrace(); // 예외 상세 출력
            return Collections.emptyList();
        }
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
                return target <= period;
            }else{
                int target = Integer.parseInt(raw.replaceAll("[^0-9]",""));
                return target <= period;
            }
        } catch (Exception e) {
            log.error("계약기간 파싱 오류: {}", periodStr, e);
        }
        return false;
    }

//  가입금액 범위 확인
    private boolean checkAmount(String amountStr, int amount){// 추천범위: 최소 50만원, 최대 사용자가 입력한 금액 * 20%

        try{
            String raw = amountStr.replaceAll("[\\s\"]", "");
            
            if(raw.contains("이상") && raw.contains("이하")){
                String[] parts = raw.split("이상|이하");
                int min = parseMoney(parts[0]);
                return amount >= min;
            }
            else if(raw.contains("이상")){
                int min = parseMoney(raw.split("이상")[0]);
                return amount >= min;
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

    // csv 파싱
    public List<Map<String, Object>> parseCsvResult(String filePath) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {

            String headerLine = br.readLine();
            if (headerLine == null) {
                return resultList; // 빈 파일 처리
            }

            String[] headers = headerLine.replace("\uFEFF", "").split(",");

            System.out.println("헤더 라인: " + Arrays.toString(headers));

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",", -1); // 빈 칸도 포함

                Map<String, Object> row = new HashMap<>();
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    String key = headers[i].trim();
                    String value = values[i].trim();

                    try {
                        row.put(key, Double.parseDouble(value));
                    } catch (NumberFormatException e) {
                        row.put(key, value); // 숫자가 아니면 문자열로
                    }
                }

                resultList.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultList;
    }
}
