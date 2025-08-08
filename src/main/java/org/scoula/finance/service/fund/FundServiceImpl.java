package org.scoula.finance.service.fund;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.finance.dto.fund.*;
import org.scoula.finance.mapper.FundMapper;
import org.scoula.finance.util.CsvUtils;
import org.scoula.finance.util.PythonExecutorUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundServiceImpl  implements FundService {
    private final FundMapper fundMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CsvUtils csvUtils;

    // 펀드 리스트 조회 (필터 포함)
    @Override
    public List<FundListDto> getFundList(FundFilterDto filter){
        return fundMapper.getFundList(filter);
    }

    // 펀드 상세 정보 조회
    @Override
    public FundDetailDto getFundDetail(Long productId){
        return fundMapper.getFundDetail(productId);
    }

    @Override
    public List<FundListDto> getFundRecommendation(){
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<FundRecommendationDto> recommendationList = fundMapper.getFundRecommendationList();

        try{
            // 투자성향을 이용해 A값을 1 ~ 50?로 설정 안정형일수록 높음
            // 1. paylaod 생성
            List<FundRequestDto> productPayload = recommendationList.stream()
                    .map(dto -> new FundRequestDto(dto.getId(), "25", dto.getFundReturnsData()))
                    .toList();
            
            // 2. input.json 저장
            File inputFile = new File("data/fund/input/input.json");
            inputFile.getParentFile().mkdirs();
            objectMapper.writeValue(inputFile, productPayload);
            System.out.printf("펀트 input.json 저장 완료" + inputFile.getAbsolutePath());

            ClassPathResource resource = new ClassPathResource("python/fund/analyze.py");
            File pythonFile = resource.getFile();
            String path = pythonFile.getAbsolutePath();

            // 3. Python 실행
            PythonExecutorUtil.runPythonScript(path);

            // 4. csv 결과 파싱
            File outputFile = new File("data/fund/output/output.csv");
            outputFile.getParentFile().mkdirs(); // 디렉토리 없으면 생성

            List<Map<String, Object>> csvResult = csvUtils.parseCsvResult(outputFile.getPath());
            System.out.println("=== Python CSV 결과 ===");
            csvResult.forEach(System.out::println);

            for(Map<String,Object> result : csvResult){
                Long strId = ((Number) result.get("id")).longValue();
                double utility = ((Number) result.get("utility")).doubleValue();

                Map<String, Object> item = new HashMap<>();
                item.put("id", strId);
                item.put("utility", utility);

                resultList.add(item);
            }

            System.out.println("resultList.size = " + resultList.size());


            resultList.sort((a,b) -> Double.compare((Double) b.get("utility"), (Double) a.get("utility")));
            List<Long> top5Names = resultList.stream()
                    .limit(5)
                    .map(r -> (Long) r.get("id"))
                    .toList();

            return fundMapper.getFundListByFundProductId(top5Names);

        } catch(Exception e){
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
