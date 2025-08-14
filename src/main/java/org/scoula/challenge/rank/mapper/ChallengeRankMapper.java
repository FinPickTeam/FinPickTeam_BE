package org.scoula.challenge.rank.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.challenge.rank.dto.ChallengeRankResponseDTO;
import org.scoula.challenge.rank.dto.ChallengeRankSnapshotResponseDTO;
import org.scoula.challenge.rank.util.ChallengeParticipantProvider.ParticipantInfo;

import java.util.List;

@Mapper
public interface ChallengeRankMapper {

    // 읽기
    List<ChallengeRankResponseDTO> getCurrentChallengeRanks(@Param("challengeId") Long challengeId);

    // (기존 방식 대비: 완전 삭제는 비상시에만 사용)
    void clearCurrentChallengeRanks(@Param("challengeId") Long challengeId);

    // (레거시) 단순 insert – 필요시 유지
    void insertChallengeRank(@Param("userChallengeId") Long userChallengeId,
                             @Param("rank") int rank,
                             @Param("actualValue") int actualValue);

    // 스냅샷
    List<ChallengeRankSnapshotResponseDTO> getChallengeRankSnapshots(@Param("month") String month);
    void insertChallengeRankSnapshot(@Param("userChallengeId") Long userChallengeId,
                                     @Param("rank") int rank,
                                     @Param("actualValue") int actualValue,
                                     @Param("month") String month);

    // 참가자 집계
    List<ParticipantInfo> getParticipantsSortedByActualValue(@Param("challengeId") Long challengeId);
    List<ParticipantInfo> getParticipantsSortedByActualValueInMonth(@Param("month") String month);

    // UPSERT & 정리 쿼리
    void upsertChallengeRank(@Param("userChallengeId") Long userChallengeId,
                             @Param("rank") int rank,
                             @Param("actualValue") int actualValue);

    void deleteRanksNotIn(@Param("challengeId") Long challengeId,
                          @Param("userChallengeIds") List<Long> userChallengeIds);
}
