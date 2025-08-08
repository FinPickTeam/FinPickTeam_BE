package org.scoula.finance.service.installment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.scoula.finance.dto.deposit.DepositRecommendationDto;
import org.scoula.finance.util.CsvUtils;
import org.scoula.finance.util.PythonExecutorUtil;
import org.scoula.finance.util.UtilityCalculator;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.scoula.finance.dto.installment.*;
import org.scoula.finance.mapper.InstallmentMapper;
import org.scoula.finance.util.SavingConditionUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstallmentServiceImpl implements InstallmentService {
    private final InstallmentMapper installmentMapper;
    private final SavingConditionUtils savingConditionUtils;
    private final CsvUtils csvUtils;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 적금 리스트 조회하기
    @Override
    public List<InstallmentListDto> getInstallmentList(InstallmentFilterDto filter){
        return installmentMapper.getInstallmentList(filter);
    }

    // 적금 상품명으로 상세 정보 조회하기
    @Override
    public InstallmentDetailDto getInstallmentDetail(int installmentProductId){
        return installmentMapper.getInstallmentDetail(installmentProductId);
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
                    .map(dto -> new InstallmentConditionPayload(dto.getId(), dto.getInstallmentPreferentialRate()))
                    .toList();
            InstallmentRequestPayload payload = new InstallmentRequestPayload(period, conditionDto, productPayloads);

            // 2. input.json 저장
            File inputFile = new File("data/installment/input/input.json");
            inputFile.getParentFile().mkdirs(); // 디렉토리 없으면 생성
            objectMapper.writeValue(inputFile, payload);
            System.out.println("input.json 저장 완료: " + inputFile.getAbsolutePath());

            ClassPathResource resource = new ClassPathResource("python/installment/analyze.py");
            File pythonFile = resource.getFile();
            String path = pythonFile.getAbsolutePath();

            // 3. Python 실행
            PythonExecutorUtil.runPythonScript(path);
            
            // 4. csv 결과 파싱
            File outputFile = new File("data/installment/output/output.csv");
            outputFile.getParentFile().mkdirs(); // 디렉토리 없으면 생성

            List<Map<String, Object>> csvResult = csvUtils.parseCsvResult(outputFile.getPath());
            System.out.println("=== Python CSV 결과 ===");
            csvResult.forEach(System.out::println);

            // 5. 유틸리티 계산
            UtilityCalculator calculator = new UtilityCalculator();
            for(Map<String, Object> row : csvResult){
                Long strId = ((Number) row.get("id")).longValue();
                double totalRate = (Double) row.get("totalRate");
                int matchedCount = ((Double) row.get("matchedCount")).intValue();

                Optional<InstallmentRecommendationDto> match = filteredList.stream()
                        .filter(dto -> {
                            return (Objects.equals(dto.getId(), strId));
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
                    result.put("id", dto.getId());
                    result.put("utility", utility);
                    resultList.add(result);
                }
            }

            System.out.println("resultList.size = " + resultList.size());

            resultList.stream()
                    .limit(5)
                    .forEach(r -> {
                        System.out.println("id: " + r.get("id") + ", utility: " + r.get("utility"));
                    });

            // 6. 정렬 후 최종 추천
            resultList.sort((a, b) -> Double.compare((Double) b.get("utility"), (Double) a.get("utility")));
            List<Long> top5Names = resultList.stream()
                    .limit(5)
                    .map(r -> (Long) r.get("id"))
                    .toList();

            return installmentMapper.getInstallmentListByProductId(top5Names);

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

}
