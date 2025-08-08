
package org.scoula.summary.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.summary.dto.AssetTotalDto;
import org.scoula.summary.dto.MonthlySpendingDto;
import org.scoula.summary.dto.AssetSummaryCompareDto;
import org.scoula.summary.service.AssetSummaryService;
import org.scoula.summary.service.SpendingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/{userId}/summary")
public class AssetSummaryController {

    private final AssetSummaryService assetSummaryService;
    private final SpendingService spendingService;

    @GetMapping("/asset-total")
    public ResponseEntity<CommonResponseDTO<AssetTotalDto>> getAssetTotal(@PathVariable Long userId) {
        BigDecimal totalAsset = assetSummaryService.getTotalAsset(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("총 자산 조회 성공", new AssetTotalDto(totalAsset)));
    }

    @GetMapping("/monthly-spending")
    public ResponseEntity<CommonResponseDTO<MonthlySpendingDto>> getMonthlySpending(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        YearMonth targetMonth = (month != null) ? month : YearMonth.now();
        BigDecimal spent = spendingService.getMonthlySpending(userId, targetMonth);
        return ResponseEntity.ok(CommonResponseDTO.success("월간 소비 합계 조회 성공", new MonthlySpendingDto(spent)));
    }

    @GetMapping("/compare")
    public ResponseEntity<CommonResponseDTO<AssetSummaryCompareDto>> getAssetSummaryCompare(@PathVariable Long userId) {
        AssetSummaryCompareDto dto = assetSummaryService.getAssetSummaryCompare(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("자산/소비 증감 조회 성공", dto));
    }
}
