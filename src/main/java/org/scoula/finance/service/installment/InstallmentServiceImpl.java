package org.scoula.finance.service.installment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.scoula.finance.util.CsvUtils;
import org.scoula.finance.util.UtilityCalculator;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.scoula.finance.dto.installment.*;
import org.scoula.finance.mapper.InstallmentMapper;
import org.scoula.finance.util.SavingConditionUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstallmentServiceImpl implements InstallmentService {
    private final InstallmentMapper installmentMapper;
    private final SavingConditionUtils savingConditionUtils;
    private final CsvUtils csvUtils;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${external.python.installment}")
    private String pythonUrl;

    // 적금 리스트 조회하기
    @Override
    public List<InstallmentListDto> getInstallmentList(InstallmentFilterDto filter){
        return installmentMapper.getInstallmentList(filter);
    }

    // 적금 상품명으로 상세 정보 조회하기
    @Override
    public InstallmentDetailDto getInstallmentDetail(String installmentProductName){
        return installmentMapper.getInstallmentDetail(installmentProductName);
    }

    //사용자 맞춤 적금 추천 상품 리스트 조회하기
    @Override
    public List<InstallmentListDto> getInstallmentRecommendationList(int amount, int period, InstallmentUserConditionDto conditionDto){
        List<InstallmentRecommendationDto> filteredList = installmentMapper.getInstallmentRecommendationList().stream()
                .filter(dto -> savingConditionUtils.checkPeriod(dto.getInstallmentContractPeriod(), period) &&
                        savingConditionUtils.checkAmount(dto.getInstallmentSubscriptionAmount(), amount))
                .toList();

        List<Map<String, Object>> resultList = new ArrayList<>();

        try{
            // 1. payload 생성
            List<InstallmentConditionPayload> productPayloads = filteredList.stream()
                    .map(dto -> new InstallmentConditionPayload(dto.getInstallmentProductName(), dto.getInstallmentPreferentialRate()))
                    .toList();
            InstallmentRequestPayload payload = new InstallmentRequestPayload(period, conditionDto, productPayloads);

            // 2. input.json 저장
            File inputFile = new File("data/installment/input/input.json");
            inputFile.getParentFile().mkdirs(); // 디렉토리 없으면 생성
            objectMapper.writeValue(inputFile, payload);
            System.out.println("input.json 저장 완료: " + inputFile.getAbsolutePath());

            // 3. Python 실행
            String pythonScriptPath = pythonUrl;
            ProcessBuilder builder = new ProcessBuilder(
                    "python",
                    pythonScriptPath
            );
            builder.redirectErrorStream(true);
            Process process = builder.start();

            // 작동 확인용
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while((line = reader.readLine()) != null){
                System.out.println("[Python] " + line);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Python 실행 실패(exit code: " + exitCode + ")");
            }
            
            // 4. csv 결과 파싱
            File outputFile = new File("data/installment/output/output.csv");
            outputFile.getParentFile().mkdirs(); // 디렉토리 없으면 생성

            List<Map<String, Object>> csvResult = csvUtils.parseCsvResult(outputFile.getPath());
            System.out.println("=== Python CSV 결과 ===");
            csvResult.forEach(System.out::println);

            // 5. 유틸리티 계산
            UtilityCalculator calculator = new UtilityCalculator();
            for(Map<String, Object> row : csvResult){
                String name = (String) row.get("name");
                double totalRate = (Double) row.get("totalRate");
                int matchedCount = ((Double) row.get("matchedCount")).intValue();

                Optional<InstallmentRecommendationDto> match = filteredList.stream()
                        .filter(dto -> {
                            boolean matched = dto.getInstallmentProductName().equals(name);
                            return matched;
                        })
                        .findFirst();

                if(match.isPresent()){
                    InstallmentRecommendationDto dto = match.get();
                    double utility = calculator.calculateUtility(
                            dto.getInstallmentBasicRate(),
                            totalRate,
                            matchedCount,
                            0.8 // 중요도 가중치 (예: 사용자 입력 기반 변경 가능)
                    );

                    Map<String, Object> result = new HashMap<>();
                    result.put("상품명", dto.getInstallmentProductName());
                    result.put("유틸리티", utility);
                    resultList.add(result);
                }
            }

            System.out.println("resultList.size = " + resultList.size());

            resultList.stream()
                    .limit(5)
                    .forEach(r -> {
                        System.out.println("상품명: " + r.get("상품명") + ", 유틸리티: " + r.get("유틸리티"));
                    });

            // 6. 정렬 후 최종 추천
            resultList.sort((a, b) -> Double.compare((Double) b.get("유틸리티"), (Double) a.get("유틸리티")));
            List<String> top5Names = resultList.stream()
                    .limit(5)
                    .map(r -> (String) r.get("상품명"))
                    .toList();

            return installmentMapper.getInstallmentListByProductName(top5Names);

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

}
