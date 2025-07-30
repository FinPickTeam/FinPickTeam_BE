package org.scoula.challenge.service;

import org.scoula.challenge.dto.ChallengeCreateRequestDTO;
import org.scoula.challenge.dto.ChallengeCreateResponseDTO;

public interface ChallengeService {
    ChallengeCreateResponseDTO createChallenge(Long userId, ChallengeCreateRequestDTO req);
}
