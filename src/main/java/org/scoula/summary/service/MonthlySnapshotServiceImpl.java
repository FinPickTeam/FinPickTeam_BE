package org.scoula.summary.service;

import lombok.RequiredArgsConstructor;
import org.scoula.summary.dto.MonthlySnapshotDto;
import org.scoula.summary.mapper.MonthlySnapshotMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.math.BigDecimal.ZERO;

@Service
@RequiredArgsConstructor
public class MonthlySnapshotServiceImpl implements MonthlySnapshotService {

    private final MonthlySnapshotMapper snapshotMapper;
    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    public MonthlySnapshotDto getOrCompute(Long userId, YearMonth month) {
        String ym = month.format(YM);
        MonthlySnapshotDto found = snapshotMapper.findSnapshot(userId, ym);
        return (found != null) ? found : recomputeAndUpsert(userId, month);
    }

    @Override
    public MonthlySnapshotDto recomputeAndUpsert(Long userId, YearMonth month) {
        String ym = month.format(YM);
        // 1) 총자산(월말 시점)
        LocalDateTime monthEnd = month.atEndOfMonth().atTime(LocalTime.MAX);
        BigDecimal totalAsset = nvl(snapshotMapper.sumMonthEndAsset(userId, monthEnd.toString()));
        // 현재월이고 월말 거래데이터가 없으면 account.balance로 대체
        if (isZero(totalAsset) && YearMonth.now().equals(month)) {
            totalAsset = nvl(snapshotMapper.sumAccountsBalanceNow(userId));
        }

        // 2) ledger 월간 수입/지출
        Map<String, BigDecimal> ie = snapshotMapper.sumLedgerIncomeExpense(userId, ym);
        BigDecimal income  = nvl(ie.get("income"));
        BigDecimal expense = nvl(ie.get("expense"));

        // 3) upsert
        snapshotMapper.upsertSnapshot(userId, ym, totalAsset, income, expense);

        return new MonthlySnapshotDto(userId, ym, totalAsset, income, expense, LocalDateTime.now());
    }

    private static BigDecimal nvl(BigDecimal v) { return v == null ? ZERO : v; }
    private static boolean isZero(BigDecimal v) { return v == null || v.signum() == 0; }
}
