package org.scoula.challenge.service;

import lombok.RequiredArgsConstructor;
import org.scoula.challenge.domain.Challenge;
import org.scoula.challenge.dto.ChallengeCreateRequestDTO;
import org.scoula.challenge.dto.ChallengeCreateResponseDTO;
import org.scoula.challenge.dto.ChallengeListResponseDTO;
import org.scoula.challenge.enums.ChallengeStatus;
import org.scoula.challenge.enums.ChallengeType;
import org.scoula.challenge.exception.*;
import org.scoula.challenge.mapper.ChallengeMapper;
import org.scoula.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

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
        challenge.setStatus(ChallengeStatus.RECRUITING);
        challenge.setGoalType("소비");

        challengeMapper.insertChallenge(challenge);

        // 6. UserChallenge 생성 (is_creator = true)
        challengeMapper.insertUserChallenge(userId, challenge.getId(), true, false, 0, false);

        // 7. 닉네임 조회
        String nickname = userMapper.findNicknameById(userId);

        return new ChallengeCreateResponseDTO(challenge.getId(), nickname);
    }

    @Override
    public List<ChallengeListResponseDTO> getChallenges(Long userId, ChallengeType type, ChallengeStatus status) {
        List<Challenge> challenges = challengeMapper.findChallenges(type, status);

        List<Long> userChallengeIds = challengeMapper.findUserChallengeIds(userId);

        return challenges.stream().map(challenge -> {
            boolean isParticipating = userChallengeIds.contains(challenge.getId());

            Double myProgress = null;
            int participants = 0;

            if (challenge.getType() == ChallengeType.PERSONAL && isParticipating) {
                myProgress = challengeMapper.getUserProgress(userId, challenge.getId());
            } else if (challenge.getType() == ChallengeType.GROUP && isParticipating) {
                myProgress = challengeMapper.getGroupAverageProgress(challenge.getId());
            } else if (challenge.getStatus() == ChallengeStatus.RECRUITING) {
                participants = challengeMapper.getParticipantsCount(challenge.getId());
            }

            return ChallengeListResponseDTO.builder()
                    .id(challenge.getId())
                    .title(challenge.getTitle())
                    .type(challenge.getType())
                    .startDate(challenge.getStartDate())
                    .endDate(challenge.getEndDate())
                    .isParticipating(isParticipating)
                    .myProgressRate(myProgress)
                    .participantsCount(participants)
                    .build();
        }).collect(Collectors.toList());
    }

}

