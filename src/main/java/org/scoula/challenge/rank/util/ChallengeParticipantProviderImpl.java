package org.scoula.challenge.rank.util;

import lombok.RequiredArgsConstructor;
import org.scoula.challenge.rank.mapper.ChallengeRankMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChallengeParticipantProviderImpl implements ChallengeParticipantProvider {

    private final ChallengeRankMapper challengeRankMapper;

    @Override
    public List<ParticipantInfo> getParticipantsSortedByActualValue(Long challengeId) {
        return challengeRankMapper.getParticipantsSortedByActualValue(challengeId);
    }

    @Override
    public List<ParticipantInfo> getParticipantsSortedByActualValueInMonth(String month) {
        return challengeRankMapper.getParticipantsSortedByActualValueInMonth(month);
    }
}
