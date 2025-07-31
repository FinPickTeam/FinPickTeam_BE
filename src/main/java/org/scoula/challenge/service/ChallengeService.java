package org.scoula.challenge.service;

import org.scoula.challenge.dto.ChallengeCreateRequestDTO;
import org.scoula.challenge.dto.ChallengeCreateResponseDTO;
import org.scoula.challenge.dto.ChallengeDetailResponseDTO;
import org.scoula.challenge.dto.ChallengeListResponseDTO;
import org.scoula.challenge.enums.ChallengeStatus;
import org.scoula.challenge.enums.ChallengeType;

import java.util.List;

public interface ChallengeService {
    ChallengeCreateResponseDTO createChallenge(Long userId, ChallengeCreateRequestDTO req);
    List<ChallengeListResponseDTO> getChallenges(Long userId, ChallengeType type, ChallengeStatus status);
    ChallengeDetailResponseDTO getChallengeDetail(Long userId, Long challengeId);
}
