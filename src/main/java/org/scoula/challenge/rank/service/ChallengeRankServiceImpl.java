package org.scoula.challenge.rank.service;

import lombok.RequiredArgsConstructor;
import org.scoula.challenge.rank.dto.ChallengeRankResponseDTO;
import org.scoula.challenge.rank.dto.ChallengeRankSnapshotResponseDTO;
import org.scoula.challenge.rank.mapper.ChallengeRankMapper;
import org.scoula.challenge.rank.util.ChallengeParticipantProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Override
    public void updateCurrentRanks(Long challengeId) {
        List<ChallengeParticipantProvider.ParticipantInfo> participants = participantProvider.getParticipantsSortedByActualValue(challengeId);
        rankMapper.clearCurrentChallengeRanks(challengeId);

        int rank = 1;
        for (ChallengeParticipantProvider.ParticipantInfo p : participants) {
            rankMapper.insertChallengeRank(p.userChallengeId(), rank++, p.actualValue());
        }
    }

    @Override
    public void snapshotMonthlyRanks(String month) {
        List<ChallengeParticipantProvider.ParticipantInfo> participants = participantProvider.getParticipantsSortedByActualValueInMonth(month);

        int rank = 1;
        for (ChallengeParticipantProvider.ParticipantInfo p : participants) {
            rankMapper.insertChallengeRankSnapshot(p.userChallengeId(), rank++, p.actualValue(), month);
        }
    }

    @Override
    public List<ChallengeRankSnapshotResponseDTO> getRankSnapshot(String month) {
        return rankMapper.getChallengeRankSnapshots(month);
    }
}
