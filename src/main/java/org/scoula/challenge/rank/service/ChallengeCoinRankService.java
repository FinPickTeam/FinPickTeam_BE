package org.scoula.challenge.rank.service;

import org.scoula.challenge.rank.dto.ChallengeCoinRankResponseDTO;
import org.scoula.challenge.rank.dto.ChallengeCoinRankSnapshotResponseDTO;

import java.util.List;

public interface ChallengeCoinRankService {
    List<ChallengeCoinRankResponseDTO> getTop5WithMyRank(Long userId);

    List<ChallengeCoinRankSnapshotResponseDTO> getTop5SnapshotWithMyRank(String month, Long userId);
    void markSnapshotAsChecked(String month, Long userId);

    // (B) 현재 달 랭킹 산정/저장
    void calculateAndSaveRanks();

    // (B) 월초: 지난달 랭킹을 snapshot에 보존
    void snapshotLastMonthRanks();

    // (C) 성공률 집계(선택)
    void recomputeUserChallengeSummaryAll();
}
