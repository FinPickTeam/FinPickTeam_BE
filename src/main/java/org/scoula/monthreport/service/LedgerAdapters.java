package org.scoula.monthreport.service;

import org.scoula.transactions.domain.Ledger;

public class LedgerAdapters {
    public static Ledger fromTx(Ledger t){
        Ledger l = new Ledger();
        l.setUserId(t.getUserId());
        l.setType("EXPENSE");
        l.setAmount(t.getAmount());
        l.setCategory(t.getCategory());
        l.setDate(t.getDate());
        l.setMerchantName(t.getMerchantName());
        return l;
    }
}
