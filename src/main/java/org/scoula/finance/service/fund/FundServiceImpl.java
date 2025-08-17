package org.scoula.finance.service.fund;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.finance.dto.fund.*;
import org.scoula.finance.mapper.FundMapper;
import org.scoula.finance.util.CsvUtils;
import org.scoula.finance.util.PythonExecutorUtil;
import org.scoula.survey.domain.SurveyVO;
import org.scoula.survey.mapper.SurveyMapper;
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
    private final SurveyMapper surveyMapper;

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
    public List<FundListDto> getFundRecommendation(Long userId){
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<FundRecommendationDto> recommendationList = fundMapper.getFundRecommendationList();
        if (recommendationList.isEmpty()) return Collections.emptyList();

        ClassPathResource res = new ClassPathResource("python/fund/analyze.py");
        PythonExecutorUtil.JobWorkspace ws = null;

        try{
            // 0) python 루트 & 워크스페이스
            File pyRoot = PythonExecutorUtil.getPyRootFrom(res);
            ws = PythonExecutorUtil.createJobWorkspace(pyRoot);

            SurveyVO vo = surveyMapper.selectById(userId);

            String userType = vo.getPropensityType();
            String A;

            if(userType.equals("안정형")){
                A = "100";
            }
            else if(userType.equals("안전추구형")){
                A = "75";
            }
            else if(userType.equals("위험중립형")){
                A = "50";
            }
            else if(userType.equals("적극투자형")){
                A = "25";
            }
            else if(userType.equals("공격투자형")){
                A = "1";
            } else {
                A = "0";
            }
            // 투자성향을 이용해 A값을 1 ~ 50?로 설정 안정형일수록 높음
            // 1. paylaod 생성
            List<FundRequestDto> productPayload = recommendationList.stream()
                    .map(dto -> new FundRequestDto(dto.getId(), A, dto.getFundReturnsData()))
                    .toList();
            
            // 2. input.json 저장
            File inputFile = ws.resolve("data/fund/input/input.json");
            ws.mkdirsFor(inputFile);
            objectMapper.writeValue(inputFile, productPayload);
            System.out.printf("펀트 input.json 저장 완료" + inputFile.getAbsolutePath());

            // 3) 실행
            File scriptFile = PythonExecutorUtil.asFileOrTemp(res);
            PythonExecutorUtil.runPythonScript(scriptFile.getAbsolutePath(), ws.root);

            // 4. csv 결과 파싱
            File outputFile = ws.resolve("data/fund/output/output.csv");
            List<Map<String, Object>> csvResult = csvUtils.parseCsvResult(outputFile.getPath());
            log.info("펀드 CSV rows: {}", csvResult.size());
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
            log.error("getFundRecommendation error", e);
            return Collections.emptyList();
        } finally {
            if (ws != null) ws.cleanupQuietly();
        }
    }
}
