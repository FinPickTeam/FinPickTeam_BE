package org.scoula.monthreport.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.monthreport.mapper.MonthReportMapper;
import org.scoula.monthreport.service.MonthReportGenerator;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.YearMonth;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthReportScheduler {

    private final MonthReportMapper monthReportMapper;
    private final MonthReportGenerator monthReportGenerator;

    @Scheduled(cron = "0 0 5 1 * ?") // 매월 1일 새벽 5시
    public void autoGenerateMonthlyReports() {
        log.info("🔥 월간 리포트 자동 생성 시작됨!");
        List<Long> userIds = monthReportMapper.findUsersWithCardTransactions();
        String prevMonth = YearMonth.now().minusMonths(1).toString();

        for (Long userId : userIds) {
            boolean alreadyExists = monthReportMapper.findExistingReportMonths(userId).contains(prevMonth);
            if (!alreadyExists) {
                log.info("✅ userId {} 리포트 생성 시작 for {}", userId, prevMonth);
                monthReportGenerator.generate(userId, prevMonth);
            } else {
                log.info("⏩ userId {} 는 이미 {} 리포트 있음", userId, prevMonth);
            }
        }
        log.info("🎉 월간 리포트 자동 생성 완료!");
    }
}
