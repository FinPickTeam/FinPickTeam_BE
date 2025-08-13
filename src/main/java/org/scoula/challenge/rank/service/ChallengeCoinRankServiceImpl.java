package org.scoula.challenge.rank.service;

import lombok.RequiredArgsConstructor;
import org.scoula.challenge.rank.dto.ChallengeCoinRankResponseDTO;
import org.scoula.challenge.rank.dto.ChallengeCoinRankSnapshotResponseDTO;
import org.scoula.challenge.rank.mapper.ChallengeCoinRankMapper;
import org.scoula.challenge.rank.mapper.CommonQueryMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallengeCoinRankServiceImpl implements ChallengeCoinRankService {

    private final ChallengeCoinRankMapper rankMapper;
    private final CommonQueryMapper commonQueryMapper;

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
        if (mine != null && top5.stream().noneMatch(r -> Objects.equals(r.getUserId(), userId))) {
            top5.add(mine);
        }
        return top5;
    }

    @Override
    public void calculateAndSaveRanks() {
        String month = currentMonth();

        // (C) 성공률 요약 갱신 (선택)
        rankMapper.recomputeUserChallengeSummaryAll();

        // (B) 대상 추출: 이번 달 월누적 > 0 인 유저
        List<Long> userIds = rankMapper.getAllUserIdsForCurrentMonthFromCoin();

        // 각 유저의 현재 달 row 조회 (rank 기준 값으로 사용)
        List<ChallengeCoinRankResponseDTO> ranks = userIds.stream()
                .map(uid -> rankMapper.getMyCoinRank(uid, month))
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparingLong(ChallengeCoinRankResponseDTO::getCumulativePoint).reversed()
                        .thenComparingInt(ChallengeCoinRankResponseDTO::getChallengeCount).reversed())
                .collect(Collectors.toList());

        for (int i = 0; i < ranks.size(); i++) {
            var r = ranks.get(i);
            rankMapper.insertOrUpdateRank(r.getUserId(), month, r.getCumulativePoint(), r.getChallengeCount(), i + 1);
        }
    }

    @Override
    public void snapshotLastMonthRanks() {
        // (B) 지난달 데이터를 스냅샷 테이블로 업서트
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
