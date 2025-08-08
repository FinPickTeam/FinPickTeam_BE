package org.scoula.monthreport.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.scoula.monthreport.domain.MonthReport;
import org.scoula.monthreport.dto.*;
import org.scoula.monthreport.mapper.MonthReportMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MonthReportReadServiceImpl implements MonthReportReadService {

    private final MonthReportMapper monthReportMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public MonthReportDetailDto getReport(Long userId, String month) {
        MonthReport report = monthReportMapper.findMonthReport(userId, month);
        if (report == null) {
            throw new IllegalArgumentException("리포트가 존재하지 않습니다.");
        }

        MonthReportDetailDto dto = new MonthReportDetailDto();
        dto.setMonth(month);
        dto.setTotalExpense(report.getTotalExpense());

        dto.setCategoryChart(parseJson(report.getCategoryChart(), new TypeReference<List<CategoryRatioDto>>() {}));
        dto.setSixMonthChart(parseJson(report.getSixMonthChart(), new TypeReference<List<MonthExpenseDto>>() {}));

        // top 3 카테고리는 categoryChart에서 상위 3개 추출
        List<CategoryRatioDto> categoryChart = dto.getCategoryChart();
        List<CategoryAmountDto> top3 = categoryChart.stream()
                .sorted((a, b) -> b.getRatio().compareTo(a.getRatio()))
                .limit(3)
                .map(c -> {
                    CategoryAmountDto a = new CategoryAmountDto();
                    a.setCategory(c.getCategory());
                    a.setAmount(calculateAmountFromRatio(report.getTotalExpense(), c.getRatio()));
                    a.setRatio(c.getRatio());
                    return a;
                }).toList();
        dto.setTop3Spending(top3);

        dto.setSpendingPatternFeedback(generateSpendingAdvice(report.getFeedback()));
        dto.setNextGoal(report.getNextGoal());

        dto.setRecommendedChallenges(buildRecommendedChallenges(report.getFeedback(), report.getTotalExpense()));

        return dto;
    }

    private <T> List<T> parseJson(String json, TypeReference<List<T>> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private BigDecimal calculateAmountFromRatio(BigDecimal total, BigDecimal ratio) {
        return total.multiply(ratio).divide(BigDecimal.valueOf(100), 0, BigDecimal.ROUND_HALF_UP);
    }

    private String generateSpendingAdvice(String feedback) {
        if (feedback.contains("식비") || feedback.contains("카페")) {
            return "다음 달 식비와 카페 지출을 약 15% 줄여보는 걸 추천드려요.";
        }
        return "안정적인 소비를 유지해보세요.";
    }

    private List<RecommendedChallengeDto> buildRecommendedChallenges(String feedback, BigDecimal totalExpense) {
        return List.of(
                new RecommendedChallengeDto("저축률 회복하기", "최소 450,000원 저축해보아요."),
                new RecommendedChallengeDto("식비 + 카페 지출 줄이기", "총합 350,000원 이하로 유지해보세요."),
                new RecommendedChallengeDto("무지출 데이 도전!", "‘무지출 데이’를 2회 이상 가져보세요.")
        );
    }
}
