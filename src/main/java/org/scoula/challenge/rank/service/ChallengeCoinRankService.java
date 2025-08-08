package org.scoula.challenge.rank.service;

import org.scoula.challenge.rank.dto.ChallengeCoinRankResponseDTO;
import org.scoula.challenge.rank.dto.ChallengeCoinRankSnapshotResponseDTO;

import java.util.List;

public interface ChallengeCoinRankService {
    List<ChallengeCoinRankResponseDTO> getTop5WithMyRank(Long userId);

    List<ChallengeCoinRankSnapshotResponseDTO> getTop5SnapshotWithMyRank(String month, Long userId); // 추가
    void markSnapshotAsChecked(String month, Long userId); // 추가

    void calculateAndSaveRanks(); // 스케줄러용
}
