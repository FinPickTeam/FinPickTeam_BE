package org.scoula.challenge.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.challenge.domain.Challenge;
import org.scoula.challenge.enums.ChallengeStatus;
import org.scoula.challenge.mapper.ChallengeMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeScheduler {

    private final ChallengeMapper challengeMapper;

    // 🚀 운영 시 실제 사용: 매일 새벽 3시
    @Scheduled(cron = "0 0 3 * * *")
    public void updateChallengeStatuses() {
        processChallengeStatusUpdate("🕒 [자동 스케줄 실행]");
    }

    // ✅ 로컬에서 수동 호출용: 테스트할 수 있게 만든 메서드
    public void updateChallengeStatusesNow() {
        processChallengeStatusUpdate("🧪 [수동 호출 실행]");
    }

    // 🧪 개발 중엔 서버 켜질 때마다 실행도 가능 (원하면 주석 제거)
    /*
    @PostConstruct
    public void initOnStartup() {
        processChallengeStatusUpdate("🚀 [서버 기동시 초기 실행]");
    }
    */

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
                challengeMapper.markChallengeSuccess(id); // 임시 성공 처리
            }
        }

        log.info("{} 챌린지 상태 업데이트 완료", mode);
    }
}
