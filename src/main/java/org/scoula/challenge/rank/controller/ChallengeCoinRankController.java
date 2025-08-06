package org.scoula.challenge.rank.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.challenge.rank.dto.ChallengeCoinRankResponseDTO;
import org.scoula.challenge.rank.dto.ChallengeCoinRankSnapshotResponseDTO;
import org.scoula.challenge.rank.service.ChallengeCoinRankService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/challenge/rank/coin")
@RequiredArgsConstructor
public class ChallengeCoinRankController {

    private final ChallengeCoinRankService rankService;

    @GetMapping
    public List<ChallengeCoinRankResponseDTO> getCoinRanking(@RequestParam Long userId) {
        return rankService.getTop5WithMyRank(userId);
    }

    @GetMapping("/snapshot")
    public List<ChallengeCoinRankSnapshotResponseDTO> getCoinRankSnapshot(@RequestParam String month, @RequestParam Long userId) {
        return rankService.getTop5SnapshotWithMyRank(month, userId);
    }

    @PatchMapping("/snapshot/check")
    public void markCoinRankSnapshotChecked(@RequestParam String month, @RequestParam Long userId) {
        rankService.markSnapshotAsChecked(month, userId);
    }

}
