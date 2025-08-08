package org.scoula.finance.service.deposit;

import lombok.RequiredArgsConstructor;
import org.scoula.finance.dto.deposit.*;
import org.scoula.finance.util.CsvUtils;
import org.scoula.finance.util.PythonExecutorUtil;
import org.scoula.finance.util.SavingConditionUtils;
import org.scoula.finance.util.UtilityCalculator;
import lombok.extern.slf4j.Slf4j;
import org.scoula.finance.mapper.DepositMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositServiceImpl implements DepositService {
    private final DepositMapper depositMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SavingConditionUtils savingConditionUtils = new SavingConditionUtils();
    private final CsvUtils csvUtils = new CsvUtils();

    //예금 전체 조회
    @Override
    public List<DepositListDto> getDeposits(DepositFilterDto filter){
            return depositMapper.getDepoistList(filter);
    }

    // 예금 상세 조회
    @Override
    public DepositDetailDto selectDepositByProductName(Long depositId) {
        return depositMapper.selectDepositByProductName(depositId);
    }

    // 예금 추천
    @Override
    public List<DepositListDto> getAllDepositRecommendations(int amount, int period, DepositUserConditionDto depositUserConditionDto) {
        List<DepositRecommendationDto> filteredList = depositMapper.selectAllDepositRecommendations().stream()
                .filter(dto -> savingConditionUtils.checkAmount(dto.getDepositSubscriptionAmount(), amount)
                        && savingConditionUtils.checkPeriod(dto.getDepositContractPeriod(), period))
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

            ClassPathResource resource = new ClassPathResource("python/deposit/analyze.py");
            File pythonFile = resource.getFile();
            String path = pythonFile.getAbsolutePath();

            // 3. Python 실행
            PythonExecutorUtil.runPythonScript(path);

            // 4. CSV 결과 파싱
            File outputFile = new File("data/output/output.csv");
            outputFile.getParentFile().mkdirs(); // 디렉토리 없으면 생성

            List<Map<String, Object>> csvResult = csvUtils.parseCsvResult(outputFile.getPath());
            System.out.println("=== Python CSV 결과 ===");
            csvResult.forEach(System.out::println);

            // 5. 유틸리티 계산
            UtilityCalculator calculator = new UtilityCalculator();
            for (Map<String, Object> row : csvResult) {
                String name = (String) row.get("name");
                double totalRate = (Double) row.get("totalRate");
                int matchedCount = ((Double) row.get("matchedCount")).intValue();

                Optional<DepositRecommendationDto> match = filteredList.stream()
                        .filter(dto -> {
                            boolean matched = dto.getDepositProductName().equals(name);
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
}
