package org.scoula.challenge.rank.service;

import lombok.RequiredArgsConstructor;
import org.scoula.challenge.rank.dto.ChallengeCoinRankResponseDTO;
import org.scoula.challenge.rank.dto.ChallengeCoinRankSnapshotResponseDTO;
import org.scoula.challenge.rank.mapper.ChallengeCoinRankMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallengeCoinRankServiceImpl implements ChallengeCoinRankService {

    private final ChallengeCoinRankMapper rankMapper;

    @Override
    public List<ChallengeCoinRankResponseDTO> getTop5WithMyRank(Long userId) {
        String currentMonth = LocalDate.now().withDayOfMonth(1).toString().substring(0, 7);
        List<ChallengeCoinRankResponseDTO> top5 = rankMapper.getTop5CoinRank(currentMonth);
        ChallengeCoinRankResponseDTO mine = rankMapper.getMyCoinRank(userId, currentMonth);

        if (mine != null && top5.stream().noneMatch(r -> r.getUserId().equals(userId))) {
            top5.add(mine);
        }

        return top5;
    }

    @Override
    public void calculateAndSaveRanks() {
        String month = LocalDate.now().minusMonths(1).withDayOfMonth(1).toString().substring(0, 7);
        List<Long> userIds = rankMapper.getAllUserIdsInMonth(month);

        List<ChallengeCoinRankResponseDTO> ranks = userIds.stream().map(userId -> {
                    ChallengeCoinRankResponseDTO dto = rankMapper.getMyCoinRank(userId, month);
                    return dto != null ? dto : null;
                }).filter(r -> r != null)
                .sorted(Comparator.comparingLong(ChallengeCoinRankResponseDTO::getCumulativePoint).reversed()
                        .thenComparingInt(ChallengeCoinRankResponseDTO::getChallengeCount).reversed())
                .collect(Collectors.toList());

        for (int i = 0; i < ranks.size(); i++) {
            ChallengeCoinRankResponseDTO r = ranks.get(i);
            rankMapper.insertOrUpdateRank(r.getUserId(), month, r.getCumulativePoint(), r.getChallengeCount(), i + 1);
        }
    }

    @Override
    public List<ChallengeCoinRankSnapshotResponseDTO> getTop5SnapshotWithMyRank(String month, Long userId) {
        return rankMapper.getCoinRankSnapshotTop5WithMyRank(month, userId);
    }

    @Override
    public void markSnapshotAsChecked(String month, Long userId) {
        rankMapper.markCoinRankSnapshotChecked(month, userId);
    }

}
