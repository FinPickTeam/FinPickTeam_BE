package org.scoula.transactions.service;

import org.scoula.account.domain.Account;
import org.scoula.transactions.dto.AccountTransactionDto;

import java.util.List;

public interface AccountTransactionService {
    List<AccountTransactionDto> getTransactions(Long userId, Long accountId, String from, String to);
    void syncAccountTransactions(Account account);
    void syncAccountTransactions(Account account, boolean isInitial);
}
