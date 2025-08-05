package org.scoula.monthreport.service;

import lombok.RequiredArgsConstructor;
import org.scoula.monthreport.mapper.MonthReportMapper;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonthReportInitServiceImpl implements MonthReportInitService {

    private final MonthReportMapper monthReportMapper;
    private final MonthReportGenerator monthReportGenerator;

    @Override
    public List<String> generateAllMissingReports(Long userId) {
        List<String> txMonths = monthReportMapper.findTransactionMonths(userId);
        if (txMonths.isEmpty()) {
            throw new NoSuchElementException("거래내역이 없어 리포트를 생성하지 않았습니다.");
        }

        List<String> existingMonths = monthReportMapper.findExistingReportMonths(userId);
        String currentMonth = YearMonth.now().toString();

        List<String> missing = txMonths.stream()
                .filter(m -> !existingMonths.contains(m))
                .filter(m -> m.compareTo(currentMonth) < 0)  // 이번 달 제외
                .sorted()
                .collect(Collectors.toList());

        if (missing.isEmpty()) {
            throw new IllegalStateException("이미 전체 리포트가 생성된 사용자입니다.");
        }

        for (String month : missing) {
            monthReportGenerator.generate(userId, month);
        }

        return missing;
    }
}
