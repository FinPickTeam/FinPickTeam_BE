package org.scoula.summary.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.summary.dto.MonthlySnapshotDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Mapper
public interface MonthlySnapshotMapper {

    MonthlySnapshotDto findSnapshot(@Param("userId") Long userId,
                                    @Param("month") String month); // "yyyy-MM"

    void upsertSnapshot(@Param("userId") Long userId,
                        @Param("month") String month,
                        @Param("totalAsset") BigDecimal totalAsset,
                        @Param("income") BigDecimal income,
                        @Param("expense") BigDecimal expense); // total_amount 컬럼에 들어감

    BigDecimal sumMonthEndAsset(@Param("userId") Long userId,
                                @Param("monthEnd") LocalDateTime monthEnd);

    BigDecimal sumAccountsBalanceNow(@Param("userId") Long userId);

    Map<String, BigDecimal> sumLedgerIncomeExpense(@Param("userId") Long userId,
                                                   @Param("month") String month); // "yyyy-MM"

    /** 백필 시작점 찾기 */
    String findEarliestLedgerYm(@Param("userId") Long userId); // "yyyy-MM" or null
}
