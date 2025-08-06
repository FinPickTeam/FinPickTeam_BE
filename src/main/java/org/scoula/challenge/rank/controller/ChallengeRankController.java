package org.scoula.challenge.rank.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.challenge.rank.dto.ChallengeRankResponseDTO;
import org.scoula.challenge.rank.dto.ChallengeRankSnapshotResponseDTO;
import org.scoula.challenge.rank.service.ChallengeRankService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/challenge/rank")
@RequiredArgsConstructor
public class ChallengeRankController {

    private final ChallengeRankService rankService;

    @GetMapping("/{challengeId}")
    public List<ChallengeRankResponseDTO> getCurrentRank(@PathVariable Long challengeId) {
        return rankService.getCurrentRank(challengeId);
    }

    @PostMapping("/{challengeId}/refresh")
    public void updateRank(@PathVariable Long challengeId) {
        rankService.updateCurrentRanks(challengeId);
    }

    @GetMapping("/snapshot")
    public List<ChallengeRankSnapshotResponseDTO> getSnapshot(@RequestParam String month) {
        return rankService.getRankSnapshot(month);
    }
}
