package org.scoula.transactions.service;

import org.scoula.transactions.dto.TransactionDTO;
import org.scoula.transactions.dto.TransactionDetailDTO;

import java.util.List;

public interface TransactionService {
    List<TransactionDTO> getTransactionsByUserId(Long userId);
    List<TransactionDTO> getTransactionsByAccountId(Long accountId); // 추가
    TransactionDetailDTO getTransactionDetail(Long id);
}
