package org.scoula.challenge.rank.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.challenge.rank.dto.ChallengeCoinRankResponseDTO;
import org.scoula.challenge.rank.dto.ChallengeCoinRankSnapshotResponseDTO;

import java.util.List;

public interface ChallengeCoinRankMapper {

    List<ChallengeCoinRankResponseDTO> getTop5CoinRank(@Param("month") String month);
    ChallengeCoinRankResponseDTO getMyCoinRank(@Param("userId") Long userId, @Param("month") String month);

    void insertOrUpdateRank(@Param("userId") Long userId,
                            @Param("month") String month,
                            @Param("cumulativePoint") Long cumulativePoint,
                            @Param("challengeCount") int challengeCount,
                            @Param("rank") int rank);

    List<Long> getAllUserIdsForCurrentMonthFromCoin();

    List<ChallengeCoinRankSnapshotResponseDTO> getCoinRankSnapshotTop5WithMyRank(@Param("month") String month,
                                                                                 @Param("userId") Long userId);
    void markCoinRankSnapshotChecked(@Param("month") String month, @Param("userId") Long userId);

    // ✅ 추가: 지난달 랭킹 → 스냅샷 업서트
    void upsertCoinRankSnapshotFromRank(@Param("month") String month);

    void recomputeUserChallengeSummaryAll();
    void upsertUserChallengeSummaryForUser(@Param("userId") Long userId);
}
