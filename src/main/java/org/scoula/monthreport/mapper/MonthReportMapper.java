package org.scoula.monthreport.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.monthreport.domain.LedgerTransaction;
import org.scoula.monthreport.domain.MonthReport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MonthReportMapper {

    List<String> findTransactionMonths(@Param("userId") Long userId);

    List<String> findExistingReportMonths(@Param("userId") Long userId);

    List<LedgerTransaction> findLedgerTransactions(@Param("userId") Long userId,
                                                   @Param("from") LocalDate from,
                                                   @Param("to") LocalDate to);

    MonthReport findMonthReport(@Param("userId") Long userId,
                                @Param("month") String month);

    List<MonthReport> findRecentMonthReportsInclusive(@Param("userId") Long userId,
                                                      @Param("currentMonth") String currentMonth,
                                                      @Param("limit") int limit);

    void insertMonthReport(@Param("userId") Long userId,
                           @Param("month") String month,
                           @Param("totalExpense") BigDecimal totalExpense,
                           @Param("totalSaving") BigDecimal totalSaving,
                           @Param("savingRate") BigDecimal savingRate,
                           @Param("compareExpense") BigDecimal compareExpense,
                           @Param("compareSaving") BigDecimal compareSaving,
                           @Param("categoryChart") String categoryChart,
                           @Param("sixMonthChart") String sixMonthChart,
                           @Param("feedback") String feedback,
                           @Param("nextGoal") String nextGoal);

    List<Long> findUsersWithCardTransactions();
}
