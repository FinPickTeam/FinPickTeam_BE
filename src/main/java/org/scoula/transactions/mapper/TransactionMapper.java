package org.scoula.transactions.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.transactions.domain.Transaction;
import org.scoula.transactions.dto.TransactionDTO;
import org.scoula.transactions.dto.TransactionDetailDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionMapper {
    List<TransactionDTO> findByUserId(Long userId);
    List<TransactionDTO> findByAccountId(Long accountId);
    TransactionDetailDTO findById(Long id);
    void updateAnalysisText(@Param("id") Long id, @Param("analysisText") String analysisText);
    List<Transaction> findRecentTransactionsByUser(@Param("userId") Long userId,
                                                   @Param("fromDate") LocalDateTime fromDate);
}