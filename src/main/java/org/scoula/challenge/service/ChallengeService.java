package org.scoula.challenge.service;

import org.scoula.challenge.dto.*;
import org.scoula.challenge.enums.ChallengeStatus;
import org.scoula.challenge.enums.ChallengeType;

import java.util.List;

public interface ChallengeService {
    ChallengeCreateResponseDTO createChallenge(Long userId, ChallengeCreateRequestDTO req);
    List<ChallengeListResponseDTO> getChallenges(Long userId, ChallengeType type, ChallengeStatus status, Boolean participating);
    ChallengeDetailResponseDTO getChallengeDetail(Long userId, Long challengeId);
    void joinChallenge(Long userId, Long challengeId, Integer password);
    ChallengeSummaryResponseDTO getChallengeSummary(Long userId);

    ChallengeResultResponseDTO getChallengeResult(Long userId, Long challengeId);
    void confirmChallengeResult(Long userId, Long challengeId);
    boolean hasUnconfirmedResult(Long userId);

}
