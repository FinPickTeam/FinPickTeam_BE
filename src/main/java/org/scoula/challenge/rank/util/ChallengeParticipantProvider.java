package org.scoula.challenge.rank.util;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

public interface ChallengeParticipantProvider {
    List<ParticipantInfo> getParticipantsSortedByActualValue(Long challengeId);

    List<ParticipantInfo> getParticipantsSortedByActualValueInMonth(String month);

    record ParticipantInfo(Long userChallengeId, int actualValue) {}
}