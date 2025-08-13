// org/scoula/challenge/rank/mapper/CommonQueryMapper.java
package org.scoula.challenge.rank.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.challenge.rank.dto.CommonChallengeSimpleDTO;

public interface CommonQueryMapper {
    CommonChallengeSimpleDTO getInProgressCommonOne();
    CommonChallengeSimpleDTO getRecruitingCommonOne();
    Integer isParticipating(@Param("userId") Long userId, @Param("challengeId") Long challengeId);
}
