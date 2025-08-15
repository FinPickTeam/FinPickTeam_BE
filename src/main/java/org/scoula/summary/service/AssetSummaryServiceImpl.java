package org.scoula.summary.service;

import lombok.RequiredArgsConstructor;
import org.scoula.summary.dto.AssetSummaryCompareDto;
import org.scoula.summary.dto.MonthlySnapshotDto;
import org.scoula.transactions.mapper.CardTransactionMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;

import static java.math.BigDecimal.ZERO;

@Service
@RequiredArgsConstructor
public class AssetSummaryServiceImpl implements AssetSummaryService {

    private final MonthlySnapshotService monthlySnapshotService;
    private final CardTransactionMapper cardTransactionMapper;

    @Override
    public BigDecimal getAccountTotal(Long userId) {
        YearMonth now = YearMonth.now();
        MonthlySnapshotDto snap = monthlySnapshotService.getOrCompute(userId, now);
        return nvl(snap.getTotalAsset());
    }

    @Override
    public BigDecimal getCardTotal(Long userId) {
        return nvl(getCardTotalForMonth(userId, YearMonth.now()));
    }

    @Override
    public BigDecimal getTotalAsset(Long userId) {
        return getAccountTotal(userId);
    }

    @Override
    public BigDecimal getAssetChange(Long userId, YearMonth month) {
        MonthlySnapshotDto cur  = monthlySnapshotService.getOrCompute(userId, month);
        MonthlySnapshotDto prev = monthlySnapshotService.getOrCompute(userId, month.minusMonths(1));
        return nvl(cur.getTotalAsset()).subtract(nvl(prev.getTotalAsset()));
    }

    @Override
    public AssetSummaryCompareDto getAssetSummaryCompare(Long userId, YearMonth month) {
        YearMonth prev = month.minusMonths(1);
        MonthlySnapshotDto curSnap  = monthlySnapshotService.getOrCompute(userId, month);
        MonthlySnapshotDto prevSnap = monthlySnapshotService.getOrCompute(userId, prev);

        BigDecimal currentAsset = nvl(curSnap.getTotalAsset());
        BigDecimal prevAsset    = nvl(prevSnap.getTotalAsset());
        BigDecimal assetDiff    = currentAsset.subtract(prevAsset); // ← 누나가 말한 그 공식

        // 소비는 그대로 거래 합(원래 요구대로)
        BigDecimal currentCardSpent = nvl(getCardTotalForMonth(userId, month));
        BigDecimal prevCardSpent    = nvl(getCardTotalForMonth(userId, prev));
        BigDecimal spendingDiff     = currentCardSpent.subtract(prevCardSpent);

        return new AssetSummaryCompareDto(
                currentAsset, prevAsset, assetDiff,
                currentCardSpent, prevCardSpent, spendingDiff
        );
    }

    /* ----- 내부 헬퍼 ----- */
    private BigDecimal getCardTotalForMonth(Long userId, YearMonth month) {
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end   = month.atEndOfMonth().atTime(LocalTime.MAX);
        BigDecimal v = cardTransactionMapper.sumMonthlySpending(userId, start, end);
        return nvl(v);
    }

    private static BigDecimal nvl(BigDecimal v) { return v == null ? ZERO : v; }
}
