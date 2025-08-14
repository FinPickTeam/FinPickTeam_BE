package org.scoula.challenge.rank.service;

import lombok.RequiredArgsConstructor;
import org.scoula.challenge.rank.dto.ChallengeCoinRankResponseDTO;
import org.scoula.challenge.rank.dto.ChallengeCoinRankSnapshotResponseDTO;
import org.scoula.challenge.rank.mapper.ChallengeCoinRankMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallengeCoinRankServiceImpl implements ChallengeCoinRankService {

    private final ChallengeCoinRankMapper rankMapper;

    private String ym(LocalDate date) {
        return date.withDayOfMonth(1).toString().substring(0, 7); // YYYY-MM
    }
    private String currentMonth() { return ym(LocalDate.now()); }
    private String previousMonth() { return ym(LocalDate.now().minusMonths(1)); }

    @Override
    public List<ChallengeCoinRankResponseDTO> getTop5WithMyRank(Long userId) {
        String month = currentMonth();
        List<ChallengeCoinRankResponseDTO> top5 = rankMapper.getTop5CoinRank(month);
        ChallengeCoinRankResponseDTO mine = rankMapper.getMyCoinRank(userId, month);

        // 내 랭킹이 top5에 없다면 추가(프론트는 rank<=5만 top에 보여주고 나머지는 내 카드에서 사용)
        if (mine != null && top5.stream().noneMatch(r -> Objects.equals(r.getUserId(), userId))) {
            top5.add(mine);
        }
        return top5;
    }

    @Override
    public void calculateAndSaveRanks() {
        String month = currentMonth();

        // (선택) 요약 재계산
        rankMapper.recomputeUserChallengeSummaryAll();

        // (핵심) coin.monthly_cumulative_amount > 0 인 유저의 당월 스탯을 직접 조회
        //  - cumulativePoint: coin.monthly_cumulative_amount
        //  - challengeCount: user_challenge_summary.total_challenges
        //  - successRate: summary 기반 계산
        List<ChallengeCoinRankResponseDTO> stats = rankMapper.selectCurrentMonthCoinStats();

        // 내림차순 정렬: 포인트 → 참여수
        List<ChallengeCoinRankResponseDTO> sorted = stats.stream()
                .sorted(Comparator
                        .comparingLong(ChallengeCoinRankResponseDTO::getCumulativePoint).reversed()
                        .thenComparingInt(ChallengeCoinRankResponseDTO::getChallengeCount).reversed())
                .collect(Collectors.toList());

        // 순위 매겨 upsert
        for (int i = 0; i < sorted.size(); i++) {
            ChallengeCoinRankResponseDTO r = sorted.get(i);
            int rank = i + 1;
            rankMapper.insertOrUpdateRank(r.getUserId(), month, r.getCumulativePoint(), r.getChallengeCount(), rank);
        }
    }

    @Override
    public void snapshotLastMonthRanks() {
        rankMapper.upsertCoinRankSnapshotFromRank(previousMonth());
    }

    @Override
    public List<ChallengeCoinRankSnapshotResponseDTO> getTop5SnapshotWithMyRank(String month, Long userId) {
        return rankMapper.getCoinRankSnapshotTop5WithMyRank(month, userId);
    }

    @Override
    public void markSnapshotAsChecked(String month, Long userId) {
        rankMapper.markCoinRankSnapshotChecked(month, userId);
    }

    @Override
    public void recomputeUserChallengeSummaryAll() {
        rankMapper.recomputeUserChallengeSummaryAll();
    }
}
