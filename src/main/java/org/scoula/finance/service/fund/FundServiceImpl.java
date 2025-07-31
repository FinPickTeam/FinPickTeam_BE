package org.scoula.finance.service.fund;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.finance.dto.fund.*;
import org.scoula.finance.mapper.FundMapper;
import org.scoula.finance.util.CsvUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundServiceImpl  implements FundService {
    private final FundMapper fundMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CsvUtils csvUtils;
    @Value("${external.python.fund}")
    private String pythonUrl;

    // 펀드 리스트 조회 (필터 포함)
    @Override
    public List<FundListDto> getFundList(FundFilterDto filter){
        return fundMapper.getFundList(filter);
    }

    // 펀드 상세 정보 조회
    @Override
    public FundDetailDto getFundDetail(String fundProductName){
        return fundMapper.getFundDetail(fundProductName);
    }

    @Override
    public List<FundListDto> getFundRecommendation(){
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<FundRecommendationDto> recommendationList = fundMapper.getFundRecommendationList();

        try{
            // 투자성향을 이용해 A값을 1 ~ 50?로 설정 안정형일수록 높음
            // 1. paylaod 생성
            List<FundRequestDto> productPayload = recommendationList.stream()
                    .map(dto -> new FundRequestDto(dto.getFundProductName(), "25", dto.getFundReturnsData()))
                    .toList();
            
            // 2. input.json 저장
            File inputFile = new File("data/fund/input/input.json");
            inputFile.getParentFile().mkdirs();
            objectMapper.writeValue(inputFile, productPayload);
            System.out.printf("펀트 input.json 저장 완료" + inputFile.getAbsolutePath());
            
            // 3. python 실행
            ProcessBuilder builder = new ProcessBuilder(
                    "python",
                    pythonUrl
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
            File outputFile = new File("data/fund/output/output.csv");
            outputFile.getParentFile().mkdirs(); // 디렉토리 없으면 생성

            List<Map<String, Object>> csvResult = csvUtils.parseCsvResult(outputFile.getPath());
            System.out.println("=== Python CSV 결과 ===");
            csvResult.forEach(System.out::println);

            for(Map<String,Object> result : csvResult){
                String name = (String) result.get("fundName");
                double utility = (Double) result.get("utility");

                Map<String, Object> item = new HashMap<>();
                item.put("fundName", name);
                item.put("utility", utility);

                resultList.add(item);
            }

            System.out.println("resultList.size = " + resultList.size());


            resultList.sort((a,b) -> Double.compare((Double) b.get("utility"), (Double) a.get("utility")));
            List<String> top5Names = resultList.stream()
                    .limit(5)
                    .map(r -> (String) r.get("fundName"))
                    .toList();

            return fundMapper.getFundListByFundProductName(top5Names);

        } catch(Exception e){
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
