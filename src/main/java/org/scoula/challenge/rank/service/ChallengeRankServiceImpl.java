package org.scoula.challenge.rank.service;

import lombok.RequiredArgsConstructor;
import org.scoula.challenge.rank.dto.ChallengeRankResponseDTO;
import org.scoula.challenge.rank.dto.ChallengeRankSnapshotResponseDTO;
import org.scoula.challenge.rank.mapper.ChallengeRankMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChallengeRankServiceImpl implements ChallengeRankService {

    private final ChallengeRankMapper rankMapper;

    @Override
    public List<ChallengeRankResponseDTO> getCurrentRank(Long challengeId) {
        return rankMapper.getCurrentChallengeRanks(challengeId);
    }

    /** UPSERT 방식: 계산→업서트→제외자 정리 */
    @Override
    @Transactional
    public void updateCurrentRanks(Long challengeId) {
        var participants = rankMapper.getParticipantsSortedByActualValue(challengeId);

        int rank = 1;
        List<Long> included = new ArrayList<>(participants.size());

        for (var p : participants) {
            rankMapper.upsertChallengeRank(p.getUserChallengeId(), rank++, p.getActualValue());
            included.add(p.getUserChallengeId());
        }

        if (!included.isEmpty()) {
            rankMapper.deleteRanksNotIn(challengeId, included);
        } else {
            rankMapper.clearCurrentChallengeRanks(challengeId);
        }
    }

    @Override
    public void snapshotMonthlyRanks(String month) {
        var participants = rankMapper.getParticipantsSortedByActualValueInMonth(month);
        int rank = 1;
        for (var p : participants) {
            rankMapper.insertChallengeRankSnapshot(p.getUserChallengeId(), rank++, p.getActualValue(), month);
        }
    }

    @Override
    public List<ChallengeRankSnapshotResponseDTO> getRankSnapshot(String month) {
        return rankMapper.getChallengeRankSnapshots(month);
    }
}
