package org.scoula.challenge.rank.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.challenge.rank.dto.ChallengeCoinRankResponseDTO;
import org.scoula.challenge.rank.dto.ChallengeCoinRankSnapshotResponseDTO;

import java.util.List;

public interface ChallengeCoinRankMapper {

    // 현재 랭킹 조회용
    List<ChallengeCoinRankResponseDTO> getTop5CoinRank(@Param("month") String month);

    ChallengeCoinRankResponseDTO getMyCoinRank(@Param("userId") Long userId, @Param("month") String month);

    void insertOrUpdateRank(
            @Param("userId") Long userId,
            @Param("month") String month,
            @Param("cumulativePoint") Long cumulativePoint,
            @Param("challengeCount") int challengeCount,
            @Param("rank") int rank
    );

    List<Long> getAllUserIdsInMonth(@Param("month") String month);

    // 누적 포인트 랭킹 스냅샷 조회 (Top 5 + 나)
    List<ChallengeCoinRankSnapshotResponseDTO> getCoinRankSnapshotTop5WithMyRank(
            @Param("month") String month,
            @Param("userId") Long userId
    );

    // 랭킹 결과 확인 여부(is_checked) 업데이트
    void markCoinRankSnapshotChecked(
            @Param("month") String month,
            @Param("userId") Long userId
    );
}
