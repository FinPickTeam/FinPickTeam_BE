package org.scoula.challenge.rank.service;

import org.scoula.challenge.rank.dto.ChallengeRankResponseDTO;
import org.scoula.challenge.rank.dto.ChallengeRankSnapshotResponseDTO;

import java.util.List;

public interface ChallengeRankService {

    List<ChallengeRankResponseDTO> getCurrentRank(Long challengeId);

    void updateCurrentRanks(Long challengeId);

    void snapshotMonthlyRanks(String month);

    List<ChallengeRankSnapshotResponseDTO> getRankSnapshot(String month);
}
