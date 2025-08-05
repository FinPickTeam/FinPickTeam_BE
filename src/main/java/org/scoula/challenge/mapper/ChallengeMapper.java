package org.scoula.challenge.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.challenge.domain.Challenge;
import org.scoula.challenge.dto.ChallengeMemberDTO;
import org.scoula.challenge.dto.ChallengeSummaryResponseDTO;
import org.scoula.challenge.enums.ChallengeStatus;
import org.scoula.challenge.enums.ChallengeType;

import java.util.List;

public interface ChallengeMapper {
    void insertChallenge(Challenge challenge);

    void insertUserChallenge(@Param("userId") Long userId,
                             @Param("challengeId") Long challengeId,
                             @Param("isCreator") boolean isCreator,
                             @Param("isCompleted") boolean isCompleted,
                             @Param("actualValue") int actualValue,
                             @Param("isSuccess") boolean isSuccess);

    int countUserOngoingChallenges(@Param("userId") Long userId, @Param("type") String type);

    List<Challenge> findChallenges(@Param("type") ChallengeType type,
                                   @Param("status") ChallengeStatus status);

    List<Long> findUserChallengeIds(@Param("userId") Long userId);

    Double getUserProgress(@Param("userId") Long userId,
                           @Param("challengeId") Long challengeId);

    Double getGroupAverageProgress(@Param("challengeId") Long challengeId);

    Challenge findChallengeById(@Param("challengeId") Long challengeId);

    boolean isUserParticipating(@Param("userId") Long userId, @Param("challengeId") Long challengeId);

    List<ChallengeMemberDTO> getGroupMembersWithAvatar(@Param("challengeId") Long challengeId);

    void incrementParticipantCount(@Param("challengeId") Long challengeId);

    void updateChallengeStatus(@Param("challengeId") Long challengeId,
                               @Param("status") String status);

    List<Challenge> findAllChallenges(); // 모든 챌린지

    void completeUserChallenges(@Param("challengeId") Long challengeId); // 유저 챌린지 완료 처리

    List<Long> findUserIdsByChallengeId(@Param("challengeId") Long challengeId);

    void updateActualValue(@Param("userId") Long userId,
                           @Param("challengeId") Long challengeId,
                           @Param("actualValue") int actualValue);

    void failUserChallenge(@Param("userId") Long userId,
                           @Param("challengeId") Long challengeId);

    void succeedUserChallenge(@Param("userId") Long userId,
                              @Param("challengeId") Long challengeId);

    String getCategoryNameById(@Param("categoryId") Long categoryId);

    ChallengeSummaryResponseDTO getChallengeSummary(@Param("userId") Long userId);

    // 유저별 참여 횟수, 성공률 등 계산하는 메서드
    void insertOrUpdateUserChallengeSummary(@Param("userId") Long userId);
    void incrementUserSuccessCount(@Param("userId") Long userId);
    void incrementUserTotalChallenges(@Param("userId") Long userId);
    void updateAchievementRate(@Param("userId") Long userId);


}

