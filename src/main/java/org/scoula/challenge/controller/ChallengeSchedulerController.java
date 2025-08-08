package org.scoula.challenge.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.challenge.scheduler.ChallengeScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@Component
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenge")
public class ChallengeSchedulerController {

    private final ChallengeScheduler challengeScheduler;

    // 🧪 수동으로 스케줄 실행해보기 (로컬 테스트용)
    @GetMapping("/scheduler/run-now")
    public String runNow() {
        challengeScheduler.updateChallengeStatusesNow();
        return "챌린지 상태 수동 업데이트 완료!";
    }

    @GetMapping("/scheduler/check-success-now")
    public String runSuccessCheckNow() {
        challengeScheduler.evaluateChallengeSuccessesNow();
        return "챌린지 성공 여부 수동 확인 완료!";
    }

}
