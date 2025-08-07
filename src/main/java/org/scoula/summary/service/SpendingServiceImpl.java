
package org.scoula.summary.service;

import lombok.RequiredArgsConstructor;
import org.scoula.transactions.mapper.CardTransactionMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class SpendingServiceImpl implements SpendingService {

    private final CardTransactionMapper cardTransactionMapper;

    @Override
    public BigDecimal getMonthlySpending(Long userId, YearMonth month) {
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.atEndOfMonth().atTime(LocalTime.MAX);
        return cardTransactionMapper.sumMonthlySpending(userId, start, end);
    }
}
