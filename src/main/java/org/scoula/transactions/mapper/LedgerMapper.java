package org.scoula.transactions.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.scoula.transactions.domain.Ledger;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface LedgerMapper {
    List<Ledger> findLedgers(@Param("userId") Long userId,
                             @Param("from") String from,
                             @Param("to") String to,
                             @Param("category") String category);

    Ledger findLedgerDetail(@Param("userId") Long userId, @Param("ledgerId") Long ledgerId);

    void accountInsert(Ledger ledger);
    void cardInsert(Ledger ledger);

    void updateLedgerCategory(@Param("ledgerId") Long ledgerId, @Param("categoryId") Long categoryId);
    void updateLedgerMemo(@Param("ledgerId") Long ledgerId, @Param("memo") String memo);
    void updateAnalysis(@Param("ledgerId") Long ledgerId, @Param("analysis") String analysis);

    // 1. 최근 N개월 카테고리별 월평균
    Double selectCategoryPrevAvg(@Param("userId") Long userId,
                                 @Param("category") String category,
                                 @Param("monthCount") int monthCount);

    // 2. 유저 월 소득 계산
    BigDecimal selectUserMonthlyIncomeByLedger(@Param("userId") Long userId,
                                               @Param("monthStart") LocalDateTime monthStart,
                                               @Param("nextMonthStart") LocalDateTime nextMonthStart);


    // 3. 해당 카테고리 거래 경험 여부 (이전 거래 1건이라도 있으면 1)
    Integer existsCategoryBefore(@Param("userId") Long userId,
                                 @Param("category") String category,
                                 @Param("before") LocalDateTime before);

    // 4. 최근 N개월간 가장 많이 쓴 카테고리 Top N
    List<String> selectTopCategories(@Param("userId") Long userId,
                                     @Param("monthCount") int monthCount);


    // 추가: 사용자/카테고리/기간합(지출) 집계
    Integer sumExpenseByUserAndCategoryBetween(@Param("userId") Long userId,
                                               @Param("category") String category,
                                               @Param("from") LocalDateTime from,
                                               @Param("to") LocalDateTime to);

}

