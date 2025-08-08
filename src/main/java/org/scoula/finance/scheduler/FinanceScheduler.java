package org.scoula.finance.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.finance.service.stock.StockService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.*;

import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class FinanceScheduler {

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final StockService stockService;

    @Scheduled(cron = "0 0 5 * * *", zone = "Asia/Seoul")
    public void runGetStockChartData() {
        log.info("[스케줄러] 오전 5시 주식 차트 데이터 갱신 시작");
        stockService.updateChartData();
    }

    @Scheduled(cron = "0 30 5 * * FRI", zone = "Asia/Seoul")
    public void runGetStockFactorData() {
        log.info("[스케줄러] 오전 5시 30분 주식 팩터 데이터 갱신 시작");

        // 오늘(분석일), 어제(영업일 보정), 시작일(끝에서 영업일 20일 전)
        LocalDate analyze = LocalDate.now(KST);
        LocalDate end = previousBusinessDay(analyze.minusDays(1));
        LocalDate start = minusBusinessDays(end, 20);

        String analyzeDate = analyze.format(YYYYMMDD);
        String resultDate  = end.format(YYYYMMDD);
        String startDate   = start.format(YYYYMMDD);

        log.info("analyzeDate={}, resultDate(끝)={}, startDate(시작)={}", analyzeDate, resultDate, startDate);

        stockService.updateFactor(analyzeDate, resultDate, startDate);
    }

    /** 어제를 넣어도 주말이면 금/목까지 당겨주는 보정 */
    private static LocalDate previousBusinessDay(LocalDate date) {
        LocalDate d = date;
        while (isWeekend(d)) {
            d = d.minusDays(1);
        }
        return d;
    }

    /** 영업일 기준으로 n일 이전 */
    public static LocalDate minusBusinessDays(LocalDate date, int businessDays) {
        LocalDate d = date;
        int left = businessDays;
        while (left > 0) {
            d = d.minusDays(1);
            if (!isWeekend(d)) {
                left--;
            }
        }
        return d;
    }

    private static boolean isWeekend(LocalDate d) {
        DayOfWeek w = d.getDayOfWeek();
        return w == DayOfWeek.SATURDAY || w == DayOfWeek.SUNDAY;
    }
}
