package org.scoula.transactions.service;

import lombok.RequiredArgsConstructor;
import org.scoula.transactions.mapper.LedgerMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisHelperImpl implements AnalysisHelper {

    private final LedgerMapper ledgerMapper;

    @Override
    public double getCategoryPrevAvg(Long userId, String category) {
        // 최근 3개월 평균, 없으면 0
        Double avg = ledgerMapper.selectCategoryPrevAvg(userId, category, 3);
        return avg != null ? avg : 0;
    }

    @Override
    public BigDecimal getUserMonthlyIncomeByLedger(Long userId, LocalDateTime monthStart, LocalDateTime nextMonthStart) {
        return ledgerMapper.selectUserMonthlyIncomeByLedger(userId, monthStart, nextMonthStart);
    }

    @Override
    public boolean hasUsedCategoryBefore(Long userId, String category, LocalDateTime before) {
        Integer cnt = ledgerMapper.existsCategoryBefore(userId, category, before);
        return cnt != null && cnt > 0;
    }

    @Override
    public List<String> getTopCategories(Long userId, int monthCount) {
        return ledgerMapper.selectTopCategories(userId, monthCount);
    }
}
