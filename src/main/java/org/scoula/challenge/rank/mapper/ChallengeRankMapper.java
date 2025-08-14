package org.scoula.challenge.rank.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.challenge.rank.dto.ChallengeRankResponseDTO;
import org.scoula.challenge.rank.dto.ChallengeRankSnapshotResponseDTO;
import org.scoula.challenge.rank.model.ParticipantInfo;

import java.util.List;

@Mapper
public interface ChallengeRankMapper {

    // 읽기
    List<ChallengeRankResponseDTO> getCurrentChallengeRanks(@Param("challengeId") Long challengeId);

    // 비상용 전체 삭제(특정 챌린지 것만)
    void clearCurrentChallengeRanks(@Param("challengeId") Long challengeId);

    // 레거시 insert (필요시 유지)
    void insertChallengeRank(@Param("userChallengeId") Long userChallengeId,
                             @Param("rank") int rank,
                             @Param("actualValue") int actualValue);

    // 스냅샷
    List<ChallengeRankSnapshotResponseDTO> getChallengeRankSnapshots(@Param("month") String month);
    void insertChallengeRankSnapshot(@Param("userChallengeId") Long userChallengeId,
                                     @Param("rank") int rank,
                                     @Param("actualValue") int actualValue,
                                     @Param("month") String month);

    // 참가자 목록 조회 (Provider 제거, Mapper 직접 사용)
    List<ParticipantInfo> getParticipantsSortedByActualValue(@Param("challengeId") Long challengeId);
    List<ParticipantInfo> getParticipantsSortedByActualValueInMonth(@Param("month") String month);

    // UPSERT & 정리
    void upsertChallengeRank(@Param("userChallengeId") Long userChallengeId,
                             @Param("rank") int rank,
                             @Param("actualValue") int actualValue);

    void deleteRanksNotIn(@Param("challengeId") Long challengeId,
                          @Param("userChallengeIds") List<Long> userChallengeIds);
}
