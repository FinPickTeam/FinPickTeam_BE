
package org.scoula.summary.service;

import java.math.BigDecimal;
import java.time.YearMonth;

public interface SpendingService {
    BigDecimal getMonthlySpending(Long userId, YearMonth month);
}
