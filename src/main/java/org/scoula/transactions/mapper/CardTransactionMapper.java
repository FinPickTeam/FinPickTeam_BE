package org.scoula.transactions.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.transactions.domain.CardTransaction;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CardTransactionMapper {
    List<CardTransaction> findCardTransactions(@Param("userId") Long userId,
                                               @Param("cardId") Long cardId,
                                               @Param("from") String from,
                                               @Param("to") String to);
    void insert(CardTransaction tx);
    void insertCardTransactions(List<CardTransaction> list);
    boolean existsByUserIdAndKey(
            @Param("userId") Long userId,
            @Param("authNumber") String authNumber,
            @Param("approvedAt") String approvedAt
    );
    LocalDateTime findLastTransactionDate(Long cardId);
}

