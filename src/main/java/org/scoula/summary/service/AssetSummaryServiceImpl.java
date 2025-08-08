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
        return accountMapper.sumBalanceByUserId(userId);
    }

    @Override
    public BigDecimal getCardTotal(Long userId) {
        // 카드 잔액이 아니라, 이번달 소비 합계라면 아래처럼
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime start = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime end = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);
        BigDecimal spending = cardTransactionMapper.sumMonthlySpending(userId, start, end);
        return spending != null ? spending : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalAsset(Long userId) {
        return getAccountTotal(userId).add(getCardTotal(userId));
    }

    // ️⬇️ 요약 + 전월대비 증감용 메서드 추가
    public AssetSummaryCompareDto getAssetSummaryCompare(Long userId) {
        YearMonth now = YearMonth.now();
        YearMonth prev = now.minusMonths(1);

        // 이번달 자산(계좌+카드)
        BigDecimal currentAsset = getAccountTotal(userId); // 계좌잔액 기준
        BigDecimal currentCardSpent = getCardTotal(userId); // 이번달 카드소비

        // 지난달 자산(계좌+카드)
        LocalDateTime prevStart = prev.atDay(1).atStartOfDay();
        LocalDateTime prevEnd = prev.atEndOfMonth().atTime(LocalTime.MAX);
        // 지난달 자산 스냅샷 없으니까 계좌잔액은 0으로. 카드소비만 비교
        BigDecimal prevAsset = BigDecimal.ZERO; // 개선 필요시 월말 자산 스냅샷 추가
        BigDecimal prevCardSpent = cardTransactionMapper.sumMonthlySpending(userId, prevStart, prevEnd);
        if(prevCardSpent == null) prevCardSpent = BigDecimal.ZERO;

        BigDecimal assetDiff = currentAsset.subtract(prevAsset);
        BigDecimal spendingDiff = currentCardSpent.subtract(prevCardSpent);

        return new AssetSummaryCompareDto(
                currentAsset, prevAsset, assetDiff,
                currentCardSpent, prevCardSpent, spendingDiff
        );
    }
}
