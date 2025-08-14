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

    // 총자산: 스냅샷 기반 (month 미지정 시 현재월)
    @GetMapping("/asset-total")
    public ResponseEntity<CommonResponseDTO<AssetTotalDto>> getAssetTotal(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        Long userId = user.getUserId();
        YearMonth target = (month != null) ? month : YearMonth.now();
        MonthlySnapshotDto snap = monthlySnapshotService.getOrCompute(userId, target);
        BigDecimal totalAsset = snap.getTotalAsset();
        return ResponseEntity.ok(
                CommonResponseDTO.success("총 자산 조회 성공", new AssetTotalDto(totalAsset))
        );
    }

    // 월간 소비 합계: 기존과 동일(카드 거래 합) or 스냅샷 total_amount로 대체 가능
    @GetMapping("/monthly-spending")
    public ResponseEntity<CommonResponseDTO<MonthlySpendingDto>> getMonthlySpending(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        Long userId = user.getUserId();
        YearMonth targetMonth = (month != null) ? month : YearMonth.now();
        // 스냅샷의 total_amount(지출)를 신뢰하고 싶다면 아래 2줄로 교체:
        // MonthlySnapshotDto snap = monthlySnapshotService.getOrCompute(userId, targetMonth);
        // BigDecimal spent = snap.getTotalAmount();

        BigDecimal spent = spendingService.getMonthlySpending(userId, targetMonth);
        return ResponseEntity.ok(
                CommonResponseDTO.success("월간 소비 합계 조회 성공", new MonthlySpendingDto(spent))
        );
    }

    // 전월 대비 비교: 스냅샷 기반(기준월 파라미터 추가)
    @GetMapping("/compare")
    public ResponseEntity<CommonResponseDTO<AssetSummaryCompareDto>> getAssetSummaryCompare(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        Long userId = user.getUserId();
        // 기존 서비스는 현재월 기준이지만, 파라미터 받도록 서비스 내부를 유지/확장해도 됨.
        // 여기서는 기존 메서드 재사용(현재월 기준) – 필요시 서비스 확장 권장.
        AssetSummaryCompareDto dto = assetSummaryService.getAssetSummaryCompare(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("자산/소비 증감 조회 성공", dto));
    }

    // 개발용: 강제 재계산 & upsert
    @PostMapping("/recompute")
    public ResponseEntity<CommonResponseDTO<MonthlySnapshotDto>> recompute(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        Long userId = user.getUserId();
        MonthlySnapshotDto snap = monthlySnapshotService.recomputeAndUpsert(userId, month);
        return ResponseEntity.ok(CommonResponseDTO.success("스냅샷 재계산/저장 완료", snap));
    }
}
