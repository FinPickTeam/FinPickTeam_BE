package org.scoula.nhapi.service;

public interface TransactionSyncService {
    int syncTransaction(String finAccount, String fromDate, String toDate);
}
