package org.scoula.summary.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.summary.dto.MonthlySnapshotDto;

import java.math.BigDecimal;
import java.util.Map;

@Mapper
public interface MonthlySnapshotMapper {

    MonthlySnapshotDto findSnapshot(@Param("userId") Long userId,
                                    @Param("month") String month);

    void upsertSnapshot(@Param("userId") Long userId,
                        @Param("month") String month,
                        @Param("totalAsset") BigDecimal totalAsset,
                        @Param("income") BigDecimal income,
                        @Param("expense") BigDecimal expense);

    BigDecimal sumMonthEndAsset(@Param("userId") Long userId,
                                @Param("monthEnd") String monthEndTs);

    BigDecimal sumAccountsBalanceNow(@Param("userId") Long userId);

    Map<String, BigDecimal> sumLedgerIncomeExpense(@Param("userId") Long userId,
                                                   @Param("month") String month);
}
