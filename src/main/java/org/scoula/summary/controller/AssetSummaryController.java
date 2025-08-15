package org.scoula.summary.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.security.account.domain.CustomUserDetails;
import org.scoula.summary.dto.AssetSummaryCompareDto;
import org.scoula.summary.dto.AssetTotalDto;
import org.scoula.summary.dto.MonthlySpendingDto;
import org.scoula.summary.dto.MonthlySnapshotDto;
import org.scoula.summary.service.AssetSummaryService;
import org.scoula.summary.service.MonthlySnapshotService;
import org.scoula.summary.service.SpendingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/summary")
public class AssetSummaryController {

    private final AssetSummaryService assetSummaryService;
    private final SpendingService spendingService;
    private final MonthlySnapshotService monthlySnapshotService;

    /** 총자산: 스냅샷 기반 (month 미지정 시 현재월) */
    @GetMapping("/asset-total")
    public ResponseEntity<CommonResponseDTO<AssetTotalDto>> getAssetTotal(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        Long userId = user.getUserId();
        YearMonth target = (month != null) ? month : YearMonth.now();
        MonthlySnapshotDto snap = monthlySnapshotService.getOrCompute(userId, target);
        BigDecimal totalAsset = snap.getTotalAsset() == null ? BigDecimal.ZERO : snap.getTotalAsset();
        return ResponseEntity.ok(CommonResponseDTO.success("총 자산 조회 성공", new AssetTotalDto(totalAsset)));
    }

    /** 월간 소비 합계: 카드 거래 합산(요구사항 유지) */
    @GetMapping("/monthly-spending")
    public ResponseEntity<CommonResponseDTO<MonthlySpendingDto>> getMonthlySpending(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        Long userId = user.getUserId();
        YearMonth targetMonth = (month != null) ? month : YearMonth.now();
        BigDecimal spent = spendingService.getMonthlySpending(userId, targetMonth);
        return ResponseEntity.ok(CommonResponseDTO.success("월간 소비 합계 조회 성공", new MonthlySpendingDto(spent)));
    }

    /** 전월 대비 비교: 기준월 파라미터 받아서 무조건 (기준월 vs 전월) */
    @GetMapping("/compare")
    public ResponseEntity<CommonResponseDTO<AssetSummaryCompareDto>> getAssetSummaryCompare(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        Long userId = user.getUserId();
        YearMonth target = (month != null) ? month : YearMonth.now();
        AssetSummaryCompareDto dto = assetSummaryService.getAssetSummaryCompare(userId, target);
        return ResponseEntity.ok(CommonResponseDTO.success("자산/소비 증감 조회 성공", dto));
    }

    /** 강제 재계산(개발/운영툴용) */
    @PostMapping("/recompute")
    public ResponseEntity<CommonResponseDTO<MonthlySnapshotDto>> recompute(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        Long userId = user.getUserId();
        MonthlySnapshotDto snap = monthlySnapshotService.recomputeAndUpsert(userId, month);
        return ResponseEntity.ok(CommonResponseDTO.success("스냅샷 재계산/저장 완료", snap));
    }

    /** 제일 옛날 달부터 현재까지 전부 계산/업서트 (백필) */
    @PostMapping("/backfill")
    public ResponseEntity<CommonResponseDTO<Integer>> backfillAll(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getUserId();
        int count = monthlySnapshotService.backfillFromEarliest(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("스냅샷 백필 완료", count));
    }
}
