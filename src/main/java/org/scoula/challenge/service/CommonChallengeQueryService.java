package org.scoula.challenge.service;

import java.util.Map;

public interface CommonChallengeQueryService {
    Map<String, Object> getCurrentCommonChallengeWithParticipation(Long userId);
}
