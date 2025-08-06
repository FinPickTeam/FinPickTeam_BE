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

    @Scheduled(cron = "0 0 5 1 * ?") // 매월 1일 새벽 5시
    public void generateMonthlyChallengeRankSnapshot() {
        String month = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        rankService.snapshotMonthlyRanks(month);
    }
}
