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
        // 기존: accountMapper.sumBalanceByUserId(userId)
        // 변경: 월 스냅샷의 total_asset 사용(기본 현재월)
        YearMonth now = YearMonth.now();
        MonthlySnapshotDto snap = monthlySnapshotService.getOrCompute(userId, now);
        return snap.getTotalAsset() != null ? snap.getTotalAsset() : ZERO;
    }

    @Override
    public BigDecimal getCardTotal(Long userId) {
        // 기존 구현 유지(카드 월간 소비 = card_transaction 기반)
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime start = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime end = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);
        BigDecimal spending = cardTransactionMapper.sumMonthlySpending(userId, start, end);
        return spending != null ? spending : ZERO;
    }

    @Override
    public BigDecimal getTotalAsset(Long userId) {
        return getAccountTotal(userId);
    }

    @Override
    public AssetSummaryCompareDto getAssetSummaryCompare(Long userId) {
        YearMonth now = YearMonth.now();
        YearMonth prev = now.minusMonths(1);

        MonthlySnapshotDto curr = monthlySnapshotService.getOrCompute(userId, now);
        MonthlySnapshotDto prevSnap = monthlySnapshotService.getOrCompute(userId, prev);

        BigDecimal currentAsset = nvl(curr.getTotalAsset());
        BigDecimal prevAsset    = nvl(prevSnap.getTotalAsset());
        BigDecimal assetDiff    = currentAsset.subtract(prevAsset);

        // 카드 소비는 card_transaction 기준(요구사항 유지)
        BigDecimal currentCardSpent = getCardTotal(userId);

        LocalDateTime prevStart = prev.atDay(1).atStartOfDay();
        LocalDateTime prevEnd   = prev.atEndOfMonth().atTime(LocalTime.MAX);
        BigDecimal prevCardSpent = nvl(cardTransactionMapper.sumMonthlySpending(userId, prevStart, prevEnd));
        BigDecimal spendingDiff  = currentCardSpent.subtract(prevCardSpent);

        return new AssetSummaryCompareDto(
                currentAsset, prevAsset, assetDiff,
                currentCardSpent, prevCardSpent, spendingDiff
        );
    }

    private static BigDecimal nvl(BigDecimal v) { return v == null ? ZERO : v; }
}
