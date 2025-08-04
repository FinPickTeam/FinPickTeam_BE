package org.scoula.challenge.service;

import lombok.RequiredArgsConstructor;
import org.scoula.challenge.domain.Challenge;
import org.scoula.challenge.dto.*;
import org.scoula.challenge.enums.ChallengeStatus;
import org.scoula.challenge.enums.ChallengeType;
import org.scoula.challenge.exception.*;
import org.scoula.challenge.exception.ChallengeLimitExceededException;
import org.scoula.challenge.exception.join.*;
import org.scoula.challenge.mapper.ChallengeMapper;
import org.scoula.common.exception.BaseException;
import org.scoula.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallengeServiceImpl implements ChallengeService {

    private final ChallengeMapper challengeMapper;
    private final UserMapper userMapper;

    @Override
    public ChallengeCreateResponseDTO createChallenge(Long userId, ChallengeCreateRequestDTO req) {
        // 1. 챌린지 참여 제한
        int count = challengeMapper.countUserOngoingChallenges(userId, req.getType().name());
        if (count >= 3) throw new ChallengeLimitExceededException(req.getType().name());

        // 2. 날짜 유효성 체크
        if (req.getStartDate().isAfter(req.getEndDate())) throw new IllegalArgumentException("시작일은 종료일보다 이후일 수 없습니다.");
        long days = ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate()) + 1;
        if (days < 3) throw new ChallengeDurationTooShortException();
        if (ChronoUnit.DAYS.between(LocalDate.now(), req.getStartDate()) > 7) throw new ChallengeStartTooLateException();

        // 3. 목표 금액 검증
        if (req.getGoalValue() < 1000) throw new ChallengeGoalTooSmallException();

        // 4. 비밀번호 검증
        if (Boolean.TRUE.equals(req.getUsePassword())) {
            if (req.getPassword() == null) {
                throw new ChallengePasswordRequiredException();
            }
            if (!String.valueOf(req.getPassword()).matches("\\d{4}")) {
                throw new ChallengePasswordFormatException();
            }
        }

        // 5. Challenge 생성
        Challenge challenge = new Challenge();
        challenge.setTitle(req.getTitle());
        challenge.setCategoryId(req.getCategoryId());
        challenge.setDescription(req.getDescription());
        challenge.setStartDate(req.getStartDate());
        challenge.setEndDate(req.getEndDate());
        challenge.setType(req.getType());
        challenge.setGoalValue(req.getGoalValue());
        challenge.setUsePassword(req.getUsePassword());
        challenge.setPassword(req.getUsePassword() ? req.getPassword() : null);
        challenge.setWriterId(userId);
        challenge.setMaxParticipants(req.getType() == ChallengeType.GROUP ? 6 : 1);
        challenge.setGoalType("소비");

        // 챌린지 상태 및 초기 참여 인원 조건 분기
        if (req.getType() == ChallengeType.PERSONAL) {
            challenge.setStatus(ChallengeStatus.CLOSED); // 개인 챌린지는 혼자 하는 거니까 바로 모집마감
            challenge.setParticipantCount(1); // 본인만 참여
        } else if (req.getType() == ChallengeType.GROUP) {
            challenge.setStatus(ChallengeStatus.RECRUITING); // 소그룹은 모집 시작
            challenge.setParticipantCount(1); // 본인 먼저 참여
        } else {
            challenge.setStatus(ChallengeStatus.RECRUITING); // 공통 챌린지도 모집 시작
            challenge.setParticipantCount(0); // 플랫폼 챌린지는 시스템 유저 제외
        }

        challengeMapper.insertChallenge(challenge);

        // 6. UserChallenge 생성 (is_creator = true)
        challengeMapper.insertUserChallenge(userId, challenge.getId(), true, false, 0, false);

        // 7. 닉네임 조회
        String nickname = userMapper.findNicknameById(userId);

        // 8. 참여관련 숫자 데이터 수정
        // 요약 테이블이 없을 수도 있으니 insert or update
        challengeMapper.insertOrUpdateUserChallengeSummary(userId);
        challengeMapper.incrementUserTotalChallenges(userId);
        challengeMapper.updateAchievementRate(userId);

        return new ChallengeCreateResponseDTO(challenge.getId(), nickname);
    }

    @Override
    public List<ChallengeListResponseDTO> getChallenges(Long userId, ChallengeType type, ChallengeStatus status) {
        List<Challenge> challenges = challengeMapper.findChallenges(type, status);

        List<Long> userChallengeIds = challengeMapper.findUserChallengeIds(userId);

        return challenges.stream().map(challenge -> {
            boolean isParticipating = userChallengeIds.contains(challenge.getId());

            Double myProgress = null;

            if (challenge.getType() == ChallengeType.PERSONAL && isParticipating) {
                myProgress = challengeMapper.getUserProgress(userId, challenge.getId());
            } else if (challenge.getType() == ChallengeType.GROUP && isParticipating) {
                myProgress = challengeMapper.getGroupAverageProgress(challenge.getId());
            }

            return ChallengeListResponseDTO.builder()
                    .id(challenge.getId())
                    .title(challenge.getTitle())
                    .type(challenge.getType())
                    .startDate(challenge.getStartDate())
                    .endDate(challenge.getEndDate())
                    .isParticipating(isParticipating)
                    .myProgressRate(myProgress)
                    .participantsCount(challenge.getParticipantCount())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public ChallengeDetailResponseDTO getChallengeDetail(Long userId, Long challengeId) {
        Challenge challenge = challengeMapper.findChallengeById(challengeId);
        if (challenge == null) throw new ChallengeNotFoundException();

        boolean isMine = challenge.getWriterId().equals(userId);
        boolean isParticipating = challengeMapper.isUserParticipating(userId, challengeId);

        Double myProgress = null;
        List<ChallengeMemberDTO> members = null;

        if (isParticipating) {
            myProgress = challengeMapper.getUserProgress(userId, challengeId);
        }

        if (challenge.getType() == ChallengeType.GROUP) {
            members = challengeMapper.getGroupMembersWithAvatar(challengeId);
        }

        return ChallengeDetailResponseDTO.builder()
                .id(challenge.getId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .type(challenge.getType())
                .status(challenge.getStatus())
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .goalType(challenge.getGoalType())
                .goalValue(challenge.getGoalValue())
                .isMine(isMine)
                .isParticipating(isParticipating)
                .myProgress(myProgress)
                .participantsCount(challenge.getParticipantCount())
                .members(members)
                .build();
    }

    @Override
    @Transactional
    public void joinChallenge(Long userId, Long challengeId, Integer password) {
        Challenge challenge = challengeMapper.findChallengeById(challengeId);
        if (challenge == null) throw new ChallengeNotFoundException();

        // 개인 챌린지는 아예 참여 자체 불가
        if (challenge.getType() == ChallengeType.PERSONAL) {
            throw new InvalidChallengeTypeJoinException(); // 아래에서 클래스 생성
        }

        // 모집 상태 확인
        if (!ChallengeStatus.RECRUITING.equals(challenge.getStatus())) {
            throw new ChallengeStatusException();
        }

        // 이미 참여 중인지 확인
        if (challengeMapper.isUserParticipating(userId, challengeId)) {
            throw new ChallengeAlreadyJoinedException();
        }

        // 참여 수 제한 체크
        int count = challengeMapper.countUserOngoingChallenges(userId, challenge.getType().name());
        if (count >= 3) {
            throw new ChallengeLimitExceededException(challenge.getType().name());
        }

        // 그룹 챌린지 전용 비밀번호 및 인원 제한 확인
        if (challenge.getType() == ChallengeType.GROUP) {
            if (Boolean.TRUE.equals(challenge.getUsePassword())) {
                if (password == null || !password.equals(challenge.getPassword())) {
                    throw new ChallengePasswordMismatchException();
                }
            }

            if (challenge.getParticipantCount() >= challenge.getMaxParticipants()) {
                throw new ChallengeFullException();
            }
        }

        // 참여 처리
        challengeMapper.insertUserChallenge(userId, challengeId, false, false, 0, false);
        challengeMapper.incrementParticipantCount(challengeId);

        if (challenge.getType() == ChallengeType.GROUP &&
                challenge.getParticipantCount() + 1 >= challenge.getMaxParticipants()) {
            challengeMapper.updateChallengeStatus(challengeId, ChallengeStatus.CLOSED.name());
        }
    }

    @Override
    public ChallengeSummaryResponseDTO getChallengeSummary(Long userId) {
        return challengeMapper.getChallengeSummary(userId);
    }


}

