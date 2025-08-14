package org.scoula.challenge.rank.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.challenge.rank.service.ChallengeCoinRankService;
import org.scoula.coin.mapper.CoinMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeCoinRankScheduler {

    private final ChallengeCoinRankService rankService;
    private final CoinMapper coinMapper;

    // 매일 05:00: 현재 달 랭킹 최신화
    @Scheduled(cron = "0 0 5 * * *", zone = "Asia/Seoul")
    public void calculateDailyCoinRank() {
        log.info("[Scheduler] 현재 달 누적 포인트 랭킹 산정 시작");
        rankService.calculateAndSaveRanks();
        log.info("[Scheduler] 현재 달 누적 포인트 랭킹 산정 완료");
    }

    // 매월 1일 05:01: 월누적 리셋
    @Scheduled(cron = "0 1 5 1 * *", zone = "Asia/Seoul")
    public void resetMonthlyCumulative() {
        log.info("[Scheduler] 월누적 포인트 리셋 시작");
        coinMapper.resetMonthlyCumulativeAll();
        log.info("[Scheduler] 월누적 포인트 리셋 완료");
    }

    // 매월 1일 05:05: 지난달 스냅샷 보존
    @Scheduled(cron = "0 5 5 1 * *", zone = "Asia/Seoul")
    public void snapshotPrevMonth() {
        log.info("[Scheduler] 지난달 누적 포인트 랭킹 스냅샷 저장 시작");
        rankService.snapshotLastMonthRanks();
        log.info("[Scheduler] 지난달 누적 포인트 랭킹 스냅샷 저장 완료");
    }
}
