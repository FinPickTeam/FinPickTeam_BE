package org.scoula.challenge.rank.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.challenge.rank.service.ChallengeCoinRankService;
import org.scoula.challenge.rank.service.ChallengeRankService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/challenge/rank/test")
@RequiredArgsConstructor
public class ChallengeRankTestController {

    private final ChallengeRankService challengeRankService;
    private final ChallengeCoinRankService coinRankService;

    // 공통 챌린지 랭킹 스냅샷 수동 생성
    @PostMapping("/common/snapshot")
    public void snapshotCommonRank(@RequestParam(required = false) String month) {
        String targetMonth = month != null ? month : LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        challengeRankService.snapshotMonthlyRanks(targetMonth);
    }

    // 누적 코인 랭킹 수동 생성
    @PostMapping("/coin/snapshot")
    public void snapshotCoinRank() {
        coinRankService.calculateAndSaveRanks();
    }
}