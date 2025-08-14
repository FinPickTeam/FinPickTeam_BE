package org.scoula.challenge.rank.service;

import lombok.RequiredArgsConstructor;
import org.scoula.challenge.rank.dto.ChallengeRankResponseDTO;
import org.scoula.challenge.rank.dto.ChallengeRankSnapshotResponseDTO;
import org.scoula.challenge.rank.mapper.ChallengeRankMapper;
import org.scoula.challenge.rank.util.ChallengeParticipantProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChallengeRankServiceImpl implements ChallengeRankService {

    private final ChallengeRankMapper rankMapper;
    private final ChallengeParticipantProvider participantProvider;

    @Override
    public List<ChallengeRankResponseDTO> getCurrentRank(Long challengeId) {
        return rankMapper.getCurrentChallengeRanks(challengeId);
    }

    /**
     * ✅ UPSERT 방식:
     *  - 계산된 참가자 목록을 rank 1부터 upsert
     *  - 이번 계산에 포함되지 않은 기존 행은 정리(deleteRanksNotIn)
     */
    @Override
    @Transactional
    public void updateCurrentRanks(Long challengeId) {
        var participants = participantProvider.getParticipantsSortedByActualValue(challengeId);

        int rank = 1;
        List<Long> included = new ArrayList<>(participants.size());

        for (var p : participants) {
            rankMapper.upsertChallengeRank(p.userChallengeId(), rank++, p.actualValue());
            included.add(p.userChallengeId());
        }

        if (!included.isEmpty()) {
            rankMapper.deleteRanksNotIn(challengeId, included);
        } else {
            // 아무도 없으면 싹 정리
            rankMapper.clearCurrentChallengeRanks(challengeId);
        }
    }

    @Override
    public void snapshotMonthlyRanks(String month) {
        var participants = participantProvider.getParticipantsSortedByActualValueInMonth(month);
        int rank = 1;
        for (var p : participants) {
            rankMapper.insertChallengeRankSnapshot(p.userChallengeId(), rank++, p.actualValue(), month);
        }
    }

    @Override
    public List<ChallengeRankSnapshotResponseDTO> getRankSnapshot(String month) {
        return rankMapper.getChallengeRankSnapshots(month);
    }
}
