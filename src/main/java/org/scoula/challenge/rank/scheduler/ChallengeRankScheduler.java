package org.scoula.challenge.rank.scheduler;

import lombok.RequiredArgsConstructor;
import org.scoula.challenge.rank.service.ChallengeRankService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class ChallengeRankScheduler {

    private final ChallengeRankService rankService;

    // (F) 매월 1일 05:00 (KST) : 지난달 공통 챌린지 랭킹 스냅샷
    @Scheduled(cron = "0 0 5 1 * *", zone = "Asia/Seoul")
    public void generateMonthlyChallengeRankSnapshot() {
        String month = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        rankService.snapshotMonthlyRanks(month);
    }
}
