package org.scoula.transactions.service;

import org.scoula.account.domain.Account;

public interface TransactionSyncProcessor {
    void syncAccountTransactions(Account account);
    void syncAccountTransactions(Account account, boolean isInitial);
}
