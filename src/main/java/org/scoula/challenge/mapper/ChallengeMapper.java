package org.scoula.challenge.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.challenge.domain.Challenge;
import org.scoula.challenge.dto.ChallengeMemberDTO;
import org.scoula.challenge.enums.ChallengeStatus;
import org.scoula.challenge.enums.ChallengeType;

import java.util.List;

@Mapper
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


}

