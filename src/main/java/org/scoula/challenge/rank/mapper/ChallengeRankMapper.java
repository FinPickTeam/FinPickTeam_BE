package org.scoula.challenge.rank.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.challenge.rank.dto.ChallengeRankResponseDTO;
import org.scoula.challenge.rank.dto.ChallengeRankSnapshotResponseDTO;
import org.scoula.challenge.rank.util.ChallengeParticipantProvider.ParticipantInfo;

import java.util.List;

public interface ChallengeRankMapper {

    List<ChallengeRankResponseDTO> getCurrentChallengeRanks(@Param("challengeId") Long challengeId);

    void clearCurrentChallengeRanks(@Param("challengeId") Long challengeId);

    void insertChallengeRank(@Param("userChallengeId") Long userChallengeId,
                             @Param("rank") int rank,
                             @Param("actualValue") int actualValue);

    List<ChallengeRankSnapshotResponseDTO> getChallengeRankSnapshots(@Param("month") String month);

    void insertChallengeRankSnapshot(@Param("userChallengeId") Long userChallengeId,
                                     @Param("rank") int rank,
                                     @Param("actualValue") int actualValue,
                                     @Param("month") String month);

    List<ParticipantInfo> getParticipantsSortedByActualValue(@Param("challengeId") Long challengeId);

    List<ParticipantInfo> getParticipantsSortedByActualValueInMonth(@Param("month") String month);
}
