package org.scoula.challenge.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.challenge.domain.Challenge;
import org.scoula.challenge.mapper.ChallengeMapper;
import org.scoula.transactions.mapper.LedgerMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeScheduler {

    private final ChallengeMapper challengeMapper;
    private final LedgerMapper ledgerMapper;

    // 운영: 매일 KST 새벽 4시 실행
    @Transactional
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void runDailyChallengeScheduler() {
        // 1) 성공/실패 먼저 (조기 실패 + 종료 후 최종 평가)
        evaluateEarlyFailsForInProgress();
        evaluateFinalForEndedChallenges();

        // 2) 상태 업데이트 (IN_PROGRESS/COMPLETED 및 user_challenge 완료 안전망)
        processChallengeStatusUpdate("🕒 [챌린지 상태 업데이트]");
    }

    /** 진행 중(IN_PROGRESS) 챌린지 대상 조기 실패 평가 */
    private void evaluateEarlyFailsForInProgress() {
        LocalDate today = LocalDate.now(); // KST 기준
        LocalDateTime to = today.atStartOfDay(); // 오늘 00:00 (스케줄은 04:00에 돎)

        List<Challenge> inProgress = challengeMapper.findInProgressChallenges();
        log.info("🔎 조기 실패 평가 대상 챌린지 수: {}", inProgress.size());

        for (Challenge ch : inProgress) {
            LocalDateTime from = ch.getStartDate().atStartOfDay();
            if (!to.isAfter(from)) continue; // 아직 기간 시작 전/동일 시점이면 스킵

            String categoryName = challengeMapper.getCategoryNameById(ch.getCategoryId());
            List<Long> users = challengeMapper.findActiveUsers(ch.getId()); // 미완료만
            for (Long userId : users) {
                int actual = ledgerMapper.sumExpenseByUserAndCategoryBetween(
                        userId, categoryName, from, to);
                challengeMapper.updateActualValue(userId, ch.getId(), actual);

                if (actual > ch.getGoalValue()) {
                    challengeMapper.failUserChallenge(userId, ch.getId()); // is_success=false, is_completed=true
                    log.info("❌ 조기 실패 처리 - userId={}, challengeId={}, actual={}, goal={}",
                            userId, ch.getId(), actual, ch.getGoalValue());
                }
            }
        }
        log.info("✅ 조기 실패 평가 완료");
    }

    /** 종료된 챌린지(어제까지 끝난) 최종 평가 */
    private void evaluateFinalForEndedChallenges() {
        List<Challenge> ended = challengeMapper.findEndedChallengesNeedingEvaluation(); // end_date < CURDATE()
        log.info("🔎 최종 평가 대상(종료) 챌린지 수: {}", ended.size());

        for (Challenge ch : ended) {
            String categoryName = challengeMapper.getCategoryNameById(ch.getCategoryId());
            List<Long> users = challengeMapper.findActiveUsers(ch.getId()); // 아직 미완료만
            LocalDateTime from = ch.getStartDate().atStartOfDay();
            LocalDateTime to = ch.getEndDate().plusDays(1).atStartOfDay(); // 기간 전체 포함

            for (Long userId : users) {
                int actual = ledgerMapper.sumExpenseByUserAndCategoryBetween(
                        userId, categoryName, from, to);
                challengeMapper.updateActualValue(userId, ch.getId(), actual);

                if (actual > ch.getGoalValue()) {
                    challengeMapper.failUserChallenge(userId, ch.getId());
                    log.info("❌ 최종 실패 - userId={}, challengeId={}, actual={}, goal={}",
                            userId, ch.getId(), actual, ch.getGoalValue());
                } else {
                    challengeMapper.succeedUserChallenge(userId, ch.getId());
                    challengeMapper.insertOrUpdateUserChallengeSummary(userId);
                    challengeMapper.incrementUserSuccessCount(userId);
                    challengeMapper.updateAchievementRate(userId);
                    log.info("🎉 최종 성공 - userId={}, challengeId={}, actual={}, goal={}",
                            userId, ch.getId(), actual, ch.getGoalValue());
                }
            }
        }
        log.info("✅ 종료 챌린지 최종 평가 완료");
    }

    /** 상태 업데이트(멱등 집합 쿼리) + user_challenge 완료 안전망 */
    private void processChallengeStatusUpdate(String mode) {
        log.info("{} 시작", mode);
        challengeMapper.setTodayToInProgress();                 // 오늘 시작 → IN_PROGRESS
        challengeMapper.setEndedToCompleted();                  // 종료 지난 챌린지 → COMPLETED
        challengeMapper.completeUserChallengesByCompletedChallenge(); // COMPLETED 챌린지 사용자 완료 반영
        log.info("{} 완료", mode);
    }

    // === 수동 테스트용 메서드 (Controller에서 호출) ===
    @Transactional
    public void updateChallengeStatusesNow() {
        processChallengeStatusUpdate("🧪 [수동 상태 업데이트]");
    }

    @Transactional
    public void evaluateChallengeSuccessesNow() {
        evaluateEarlyFailsForInProgress();
        evaluateFinalForEndedChallenges();
    }
}
