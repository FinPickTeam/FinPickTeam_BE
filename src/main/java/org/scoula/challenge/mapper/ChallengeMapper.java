package org.scoula.challenge.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.challenge.domain.Challenge;
import org.scoula.challenge.dto.ChallengeHistoryItemDTO;
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
                             @Param("isSuccess") Boolean isSuccess);

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

    List<Challenge> findAllChallenges();

    void completeUserChallenges(@Param("challengeId") Long challengeId);

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

    void insertOrUpdateUserChallengeSummary(@Param("userId") Long userId);
    void incrementUserSuccessCount(@Param("userId") Long userId);
    void incrementUserTotalChallenges(@Param("userId") Long userId);
    void updateAchievementRate(@Param("userId") Long userId);

    int getActualValue(@Param("userId") Long userId, @Param("challengeId") Long challengeId);
    Integer getActualRewardPoint(@Param("userId") Long userId, @Param("challengeId") Long challengeId);
    void markResultChecked(@Param("userId") Long userId, @Param("challengeId") Long challengeId);
    boolean existsUnconfirmedCompletedChallenge(@Param("userId") Long userId);
    Boolean isResultChecked(@Param("userId") Long userId, @Param("challengeId") Long challengeId);

    int countSuccessMembers(@Param("challengeId") Long challengeId);

    void saveActualRewardPoint(@Param("userId") Long userId,
                               @Param("challengeId") Long challengeId,
                               @Param("actualRewardPoint") int actualRewardPoint);

    List<ChallengeHistoryItemDTO> findCompletedHistoryByUser(@Param("userId") Long userId);

    Boolean getIsSuccess(@Param("userId") Long userId, @Param("challengeId") Long challengeId);

    // ====== ⬇️ 추가 메서드(평가/상태 일괄 처리용) ⬇️ ======
    List<Challenge> findInProgressChallenges();
    List<Long> findActiveUsers(@Param("challengeId") Long challengeId);

    List<Challenge> findEndedChallengesNeedingEvaluation();

    void setTodayToInProgress();
    void setEndedToCompleted();
    void completeUserChallengesByCompletedChallenge();
}
