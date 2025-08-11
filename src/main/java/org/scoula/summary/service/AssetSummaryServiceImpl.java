package org.scoula.summary.service;

import lombok.RequiredArgsConstructor;
import org.scoula.account.mapper.AccountMapper;
import org.scoula.transactions.mapper.CardTransactionMapper;
import org.scoula.summary.dto.AssetSummaryCompareDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class AssetSummaryServiceImpl implements AssetSummaryService {

    private final AccountMapper accountMapper;
    private final CardTransactionMapper cardTransactionMapper;

    @Override
    public BigDecimal getAccountTotal(Long userId) {
        BigDecimal v = accountMapper.sumBalanceByUserId(userId);
        return v != null ? v : BigDecimal.ZERO; // ✅ null-safe
    }

    @Override
    public BigDecimal getCardTotal(Long userId) {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime start = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime end = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);
        BigDecimal spending = cardTransactionMapper.sumMonthlySpending(userId, start, end);
        return spending != null ? spending : BigDecimal.ZERO; // ✅ null-safe
    }


    @Override
    public BigDecimal getTotalAsset(Long userId) {
        return getAccountTotal(userId);
    }

    // ️⬇️ 요약 + 전월대비 증감용 메서드 추가
    public AssetSummaryCompareDto getAssetSummaryCompare(Long userId) {
        YearMonth now = YearMonth.now();
        YearMonth prev = now.minusMonths(1);

        BigDecimal currentAsset = getAccountTotal(userId);      // 이번달 자산(계좌)
        BigDecimal currentCardSpent = getCardTotal(userId);     // 이번달 카드소비

        LocalDateTime prevStart = prev.atDay(1).atStartOfDay();
        LocalDateTime prevEnd = prev.atEndOfMonth().atTime(LocalTime.MAX);

        BigDecimal prevAsset = BigDecimal.ZERO; // (향후 스냅샷 넣으면 개선)
        BigDecimal prevCardSpent = cardTransactionMapper.sumMonthlySpending(userId, prevStart, prevEnd);
        if (prevCardSpent == null) prevCardSpent = BigDecimal.ZERO;

        BigDecimal assetDiff = currentAsset.subtract(prevAsset);
        BigDecimal spendingDiff = currentCardSpent.subtract(prevCardSpent);

        return new AssetSummaryCompareDto(
                currentAsset, prevAsset, assetDiff,
                currentCardSpent, prevCardSpent, spendingDiff
        );
    }
}
