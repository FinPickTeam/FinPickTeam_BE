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

    // (기존) 사용하지 않아도 되지만 남겨둠
    List<Long> getAllUserIdsForCurrentMonthFromCoin();

    List<ChallengeCoinRankSnapshotResponseDTO> getCoinRankSnapshotTop5WithMyRank(@Param("month") String month,
                                                                                 @Param("userId") Long userId);
    void markCoinRankSnapshotChecked(@Param("month") String month, @Param("userId") Long userId);

    void upsertCoinRankSnapshotFromRank(@Param("month") String month);

    void recomputeUserChallengeSummaryAll();
    void upsertUserChallengeSummaryForUser(@Param("userId") Long userId);

    // 신규: coin + summary에서 당월 스탯을 직접 가져온다
    List<ChallengeCoinRankResponseDTO> selectCurrentMonthCoinStats();

    /** 이벤트 기반: 특정 유저의 당월 row upsert */
    int upsertRankRowForUserCurrentMonth(@Param("userId") Long userId, @Param("month") String month);

    /** 이벤트 기반: 해당 월 전체 랭크를 윈도우 함수로 재계산 */
    int refreshRanksForMonth(@Param("month") String month);
}
