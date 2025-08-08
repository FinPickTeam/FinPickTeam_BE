package org.scoula.challenge.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.challenge.domain.Challenge;
import org.scoula.challenge.enums.ChallengeStatus;
import org.scoula.challenge.mapper.ChallengeMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.scoula.common.client.LedgerClient;

//import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeScheduler {

    private final ChallengeMapper challengeMapper;
    private final LedgerClient ledgerClient;

    // 🧪 개발 중엔 서버 켜질 때마다 실행도 가능 (원하면 주석 제거)
    /*
    @PostConstruct
    public void initOnStartup() {
        processChallengeStatusUpdate("🚀 [서버 기동시 초기 실행]");
        processChallengeSuccessCheck("🕒 [챌린지 성공/실패 자동 평가]");
    }
    */

    // 🚀 운영 시 실제 사용: 매일 새벽 4시
    @Scheduled(cron = "0 0 4 * * *")
    public void runDailyChallengeScheduler() {
        // 1. 성공/실패 먼저 판단
        processChallengeSuccessCheck("🕒 [챌린지 성공/실패 판별]");

        // 2. 상태 업데이트 (COMPLETED 처리)
        processChallengeStatusUpdate("🕒 [챌린지 상태 업데이트]");
    }


    // ✅ 로컬에서 수동 호출용: 테스트할 수 있게 만든 메서드
    public void updateChallengeStatusesNow() {
        processChallengeStatusUpdate("🧪 [수동 호출 실행]");
    }

    private void processChallengeStatusUpdate(String mode) {
        LocalDate today = LocalDate.now();
        log.info("{} 챌린지 상태 업데이트 시작 - {}", mode, today);

        List<Challenge> challenges = challengeMapper.findAllChallenges();

        for (Challenge challenge : challenges) {
            Long id = challenge.getId();
            ChallengeStatus currentStatus = challenge.getStatus();
            LocalDate startDate = challenge.getStartDate();
            LocalDate endDate = challenge.getEndDate();

            if (currentStatus != ChallengeStatus.IN_PROGRESS && today.isEqual(startDate)) {
                challengeMapper.updateChallengeStatus(id, ChallengeStatus.IN_PROGRESS.name());
                log.info("🔄 챌린지 시작 처리 - ID: {}", id);
            }

            if (currentStatus != ChallengeStatus.COMPLETED && today.isAfter(endDate)) {
                challengeMapper.updateChallengeStatus(id, ChallengeStatus.COMPLETED.name());
                log.info("✅ 챌린지 완료 처리 - ID: {}", id);

                // 유저 챌린지도 완료 처리
                challengeMapper.completeUserChallenges(id);
            }
        }

        log.info("{} 챌린지 상태 업데이트 완료", mode);
    }

    public void evaluateChallengeSuccessesNow() {
        processChallengeSuccessCheck("🧪 [수동 챌린지 평가 테스트]");
    }

    private void processChallengeSuccessCheck(String mode) {
        LocalDate today = LocalDate.now();
        log.info("{} 챌린지 성공/실패 평가 시작 - {}", mode, today);

        List<Challenge> allChallenges = challengeMapper.findAllChallenges();

        for (Challenge challenge : allChallenges) {
            if (challenge.getStatus() != ChallengeStatus.IN_PROGRESS) continue;

            List<Long> userIds = challengeMapper.findUserIdsByChallengeId(challenge.getId());
            String categoryName = challengeMapper.getCategoryNameById(challenge.getCategoryId());

            for (Long userId : userIds) {
                int actualAmount = ledgerClient.getTotalExpense(
                        userId,
                        categoryName,
                        challenge.getStartDate(),
                        challenge.getEndDate()
                );

                // actual_value 업데이트
                challengeMapper.updateActualValue(userId, challenge.getId(), actualAmount);

                if (actualAmount > challenge.getGoalValue()) {
                    // 실패 처리
                    challengeMapper.failUserChallenge(userId, challenge.getId());
                    log.info("❌ 조기 실패 처리 - 유저ID: {}, 챌린지ID: {}", userId, challenge.getId());
                } else if (today.isAfter(challenge.getEndDate())) {
                    // 성공 처리
                    challengeMapper.succeedUserChallenge(userId, challenge.getId());
                    log.info("🎉 성공 처리 - 유저ID: {}, 챌린지ID: {}", userId, challenge.getId());

                    // ✅ 성공 횟수 +1 및 성공률 갱신
                    challengeMapper.insertOrUpdateUserChallengeSummary(userId);
                    challengeMapper.incrementUserSuccessCount(userId);
                    challengeMapper.updateAchievementRate(userId);
                }

            }
        }

        log.info("{} 챌린지 성공/실패 평가 완료", mode);
    }

}
