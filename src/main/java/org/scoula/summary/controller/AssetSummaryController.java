package org.scoula.summary.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.security.account.domain.CustomUserDetails;
import org.scoula.summary.dto.AssetTotalDto;
import org.scoula.summary.dto.MonthlySpendingDto;
import org.scoula.summary.dto.AssetSummaryCompareDto;
import org.scoula.summary.service.AssetSummaryService;
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

    @GetMapping("/asset-total")
    public ResponseEntity<CommonResponseDTO<AssetTotalDto>> getAssetTotal(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getUserId();
        BigDecimal totalAsset = assetSummaryService.getTotalAsset(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("총 자산 조회 성공", new AssetTotalDto(totalAsset)));
    }

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

    @GetMapping("/compare")
    public ResponseEntity<CommonResponseDTO<AssetSummaryCompareDto>> getAssetSummaryCompare(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getUserId();
        AssetSummaryCompareDto dto = assetSummaryService.getAssetSummaryCompare(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("자산/소비 증감 조회 성공", dto));
    }
}
