package org.scoula.transactions.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.transactions.domain.CardTransaction;

import java.util.List;

@Mapper
public interface CardTransactionMapper {
    List<CardTransaction> findByUserAndCard(@Param("userId") Long userId, @Param("cardId") Long cardId);
}

