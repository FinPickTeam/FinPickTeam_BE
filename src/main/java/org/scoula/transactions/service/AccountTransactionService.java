package org.scoula.transactions.service;

import org.scoula.transactions.dto.AccountTransactionDto;

import java.util.List;

public interface AccountTransactionService {
    List<AccountTransactionDto> getAccountTransactions(Long userId, Long accountId);
}
