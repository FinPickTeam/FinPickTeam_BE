package org.scoula.challenge.rank.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.challenge.rank.service.ChallengeCoinRankService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeCoinRankScheduler {

    private final ChallengeCoinRankService rankService;

    // 매월 1일 05:00에 실행
    @Scheduled(cron = "0 0 5 1 * *", zone = "Asia/Seoul")
    public void calculateMonthlyCoinRank() {
        log.info("[Scheduler] 월간 누적 포인트 랭킹 산정 시작");
        rankService.calculateAndSaveRanks();
        log.info("[Scheduler] 월간 누적 포인트 랭킹 산정 완료");
    }
}
