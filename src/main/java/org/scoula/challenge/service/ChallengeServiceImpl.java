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
import org.scoula.coin.exception.InsufficientCoinException;
import org.scoula.coin.mapper.CoinMapper;
import org.scoula.finance.dto.stock.StockListDto;
import org.scoula.finance.service.stock.StockService;
import org.scoula.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class ChallengeServiceImpl implements ChallengeService {

    private final ChallengeMapper challengeMapper;
    private final UserMapper userMapper;
    private final CoinMapper coinMapper;
    private final StockService stockService; // 내부 서비스 직접 호출 (액세스 토큰 전달 제거)

    @Override
    public ChallengeCreateResponseDTO createChallenge(Long userId, ChallengeCreateRequestDTO req) {
        // 1. 챌린지 참여 제한
        int count = challengeMapper.countUserOngoingChallenges(userId, req.getType().name());
        if (count >= 3) throw new ChallengeLimitExceededException(req.getType().name());

        // 2. 날짜 유효성 체크
        if (req.getStartDate().isAfter(req.getEndDate())) {
            throw new StartDateAfterEndDateException();
        }
        long days = ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate()) + 1;
        if (days < 3) {
            throw new ChallengeDurationTooShortException();
        }
        if (days > 30) {
            throw new ChallengeDurationTooLongException();
        }
        if (ChronoUnit.DAYS.between(LocalDate.now(), req.getStartDate()) > 7) {
            throw new ChallengeStartTooLateException();
        }


        // 3. 목표 금액 검증
        if (req.getGoalValue() < 1000) throw new ChallengeGoalTooSmallException();
        if (req.getGoalValue() > 10000000) throw new ChallengeGoalTooBigException();

        // 4. 비밀번호 검증
        if (Boolean.TRUE.equals(req.getUsePassword())) {
            if (req.getPassword() == null) {
                throw new ChallengePasswordRequiredException();
            }
            if (!String.valueOf(req.getPassword()).matches("\\d{4}")) {
                throw new ChallengePasswordFormatException();
            }
        }

        // 5. 기본 포인트 계산
        int rewardPoint;
        switch (req.getType()) {
            case PERSONAL:
                rewardPoint = 10 * (int) days;
                break;
            case GROUP:
                rewardPoint = 20 * (int) days;
                break;
            case COMMON:
                rewardPoint = 600; // 고정
                break;
            default:
                rewardPoint = 0;
        }

        // 참가비 공제 (GROUP)
        if (req.getType() != ChallengeType.PERSONAL) {
            int coin = coinMapper.getUserCoin(userId);
            if (coin < 100) throw new InsufficientCoinException();

            coinMapper.subtractCoin(userId, 100);
            coinMapper.insertCoinHistory(userId, 100, "minus", "CHALLENGE");
        }

        // 6. Challenge 생성
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
        challenge.setRewardPoint(rewardPoint);

        // 챌린지 상태 및 초기 참여 인원 조건 분기
        if (req.getType() == ChallengeType.PERSONAL) {
            System.out.println("개인 챌린지 생성");
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

        // 7. UserChallenge 생성 (is_creator = true)
        challengeMapper.insertUserChallenge(userId, challenge.getId(), true, false, 0, false);

        // 8. 닉네임 조회
        String nickname = userMapper.findNicknameById(userId);

        // 9. 참여관련 숫자 데이터 수정
        // 요약 테이블이 없을 수도 있으니 insert or update
        challengeMapper.insertOrUpdateUserChallengeSummary(userId);
        challengeMapper.incrementUserTotalChallenges(userId);
        challengeMapper.updateAchievementRate(userId);

        return new ChallengeCreateResponseDTO(challenge.getId(), nickname);
    }

    @Override
    public List<ChallengeListResponseDTO> getChallenges(Long userId, ChallengeType type, ChallengeStatus status, Boolean participating) {
        List<Challenge> challenges = challengeMapper.findChallenges(type, status);
        List<Long> userChallengeIds = challengeMapper.findUserChallengeIds(userId);

        return challenges.stream()
                .filter(challenge -> {
                    boolean isParticipating = userChallengeIds.contains(challenge.getId());

                    if (participating == null) return true; // 필터 안 씀
                    if (participating) return isParticipating; // 참여한 챌린지만
                    else return !isParticipating;              // 참여 안 한 챌린지만
                })
                .map(challenge -> {
                    boolean isParticipating = userChallengeIds.contains(challenge.getId());
                    String categoryName = challengeMapper.getCategoryNameById(challenge.getCategoryId());

                    // 참여한 챌린지인 경우만 결과 확인 여부 조회
                    Boolean resultChecked = false;
                    if (isParticipating) {
                        resultChecked = Boolean.TRUE.equals(challengeMapper.isResultChecked(userId, challenge.getId()));
                    }

                    Double myProgress = null;
                    if (isParticipating) {
                        if (challenge.getType() == ChallengeType.PERSONAL) {
                            myProgress = challengeMapper.getUserProgress(userId, challenge.getId());
                        } else if (challenge.getType() == ChallengeType.GROUP) {
                            myProgress = challengeMapper.getGroupAverageProgress(challenge.getId());
                        }
                    }

                    return ChallengeListResponseDTO.builder()
                            .id(challenge.getId())
                            .title(challenge.getTitle())
                            .categoryName(categoryName)
                            .type(challenge.getType())
                            .startDate(challenge.getStartDate())
                            .endDate(challenge.getEndDate())
                            .isParticipating(isParticipating)
                            .myProgressRate(myProgress)
                            .participantsCount(challenge.getParticipantCount())
                            .isResultCheck(resultChecked)
                            .build();
                }).collect(Collectors.toList());
    }



    @Override
    public ChallengeDetailResponseDTO getChallengeDetail(Long userId, Long challengeId) {
        Challenge challenge = challengeMapper.findChallengeById(challengeId);
        if (challenge == null) throw new ChallengeNotFoundException();

        boolean isMine = challenge.getWriterId().equals(userId);
        boolean isParticipating = challengeMapper.isUserParticipating(userId, challengeId);

        // 참여한 경우만 조회, 아니면 기본값 false
        Boolean resultChecked = false;
        if (isParticipating) {
            resultChecked = Boolean.TRUE.equals(challengeMapper.isResultChecked(userId, challenge.getId()));
        }

        Double myProgress = null;
        List<ChallengeMemberDTO> members = null;

        if (isParticipating) {
            myProgress = challengeMapper.getUserProgress(userId, challengeId);
        }

        if (challenge.getType() == ChallengeType.GROUP) {
            members = challengeMapper.getGroupMembersWithAvatar(challengeId);
        }

        String categoryName = challengeMapper.getCategoryNameById(challenge.getCategoryId());

        return ChallengeDetailResponseDTO.builder()
                .id(challenge.getId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .type(challenge.getType())
                .categoryName(categoryName)
                .status(challenge.getStatus())
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .goalType(challenge.getGoalType())
                .goalValue(challenge.getGoalValue())
                .isMine(isMine)
                .isParticipating(isParticipating)
                .myProgress(myProgress)
                .participantsCount(challenge.getParticipantCount())
                .isResultCheck(resultChecked)
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

        // 챌린지 참여 비용 확인
        if (challenge.getType() != ChallengeType.PERSONAL) {
            int coin = coinMapper.getUserCoin(userId);
            if (coin < 100) throw new InsufficientCoinException();

            coinMapper.subtractCoin(userId, 100);
            coinMapper.insertCoinHistory(userId, 100, "minus", "CHALLENGE");
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


    // 챌린지 결과 확인 관련 로직 + 내부 StockService 직접 호출
    @Override
    public ChallengeResultResponseDTO getChallengeResult(Long userId, Long challengeId)
    {
        Challenge challenge = challengeMapper.findChallengeById(challengeId);
        if (challenge == null) throw new ChallengeNotFoundException();

        // 목표 금액과 소비 금액
        int actual = challengeMapper.getActualValue(userId, challengeId);
        int goal = challenge.getGoalValue();

        // 기본 포인트와 최종 받는 포인트
        int baseReward = challenge.getRewardPoint();
        int finalReward = baseReward;

        // 포인트 계산 로직
        // GROUP 챌린지 포인트 n빵 로직 (예외 방지 + 반올림 처리)
        // PERSONAL은 baseReward 그대로
        // COMMON 챌린지는 baseReward = 600으로 고정
        // 이미 baseReward에 저장되어 있으므로 별도 계산 불필요
        if (challenge.getType() == ChallengeType.GROUP) {
            int successMembers = challengeMapper.countSuccessMembers(challenge.getId());

            if (successMembers > 0) {
                int totalEntryFee = 100 * challenge.getParticipantCount();
                int bonus = (int) Math.round((double) totalEntryFee / successMembers);
                finalReward += bonus;
            }
            // 성공자가 0명이면 추가 보상 없음 (기본 포인트만 유지)
        }

        // actual_reward_point 저장
        challengeMapper.saveActualRewardPoint(userId, challengeId, finalReward);

        // coin 지급 및 history 기록
        coinMapper.addCoinAmount(userId, finalReward);
        coinMapper.insertCoinHistory(userId, finalReward, "plus", "CHALLENGE");

        int savedAmount = goal - actual;

        // 🟡 추천 주식: StockService 직접 호출 (유저 토큰 X)
        StockRecommendationDTO bestStock = null;
        if (savedAmount > 1000) {
            List<StockListDto> stocks = stockService.getStockRecommendationList(userId, 5, savedAmount);

            // amount 이하에서 가장 비싼 것 선택
            StockListDto best = stocks.stream()
                    .filter(s -> s.getStockPrice() > 0 && s.getStockPrice() <= savedAmount) // null 체크 제거, 값 검증만
                    .max(Comparator.comparingInt(StockListDto::getStockPrice))
                    .orElse(null);

            if (best != null) {
                StockRecommendationDTO dto = new StockRecommendationDTO();
                dto.setStockCode(best.getStockCode());
                dto.setStockName(best.getStockName());
                dto.setStockPrice(best.getStockPrice());
                dto.setStockSummary(best.getStockSummary());
                bestStock = dto;
            }

        }

        if (actual < goal) {
            return ChallengeResultResponseDTO.builder()
                    .resultType("SUCCESS_WIN")
                    .actualRewardPoint(finalReward)
                    .savedAmount(savedAmount)
                    .stockRecommendation(bestStock)
                    .build();
        } else if (actual == goal) {
            return ChallengeResultResponseDTO.builder()
                    .resultType("SUCCESS_EQUAL")
                    .actualRewardPoint(finalReward)
                    .savedAmount(0)
                    .build();
        } else {
            return ChallengeResultResponseDTO.builder()
                    .resultType("FAIL")
                    .actualRewardPoint(0)
                    .savedAmount(0)
                    .build();
        }
    }

    @Override
    public void confirmChallengeResult(Long userId, Long challengeId) {
        challengeMapper.markResultChecked(userId, challengeId);
    }

    @Override
    public boolean hasUnconfirmedResult(Long userId) {
        return challengeMapper.existsUnconfirmedCompletedChallenge(userId);
    }

}

