package org.scoula.transactions.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.transactions.domain.CardTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CardTransactionMapper {
    List<CardTransaction> findCardTransactions(@Param("userId") Long userId,
                                               @Param("cardId") Long cardId,
                                               @Param("from") String from,
                                               @Param("to") String to);
    void insert(CardTransaction tx);
    boolean existsByUserIdAndCardIdAndKey(
            @Param("userId") Long userId,
            @Param("cardId") Long cardId,
            @Param("authNumber") String authNumber,
            @Param("approvedAt") java.time.LocalDateTime approvedAt
    );
    LocalDateTime findLastTransactionDate(Long cardId);
    BigDecimal sumMonthlySpending(@Param("userId") Long userId,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);
    BigDecimal sumMonthlySpendingByCard(@Param("userId") Long userId, @Param("cardId") Long cardId, @Param("start") String start, @Param("end") String end);

}

