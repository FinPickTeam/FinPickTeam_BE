package org.scoula.transactions.service;

import lombok.RequiredArgsConstructor;
import org.scoula.transactions.mapper.LedgerMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LedgerEditService {

    private final LedgerMapper ledgerMapper;

    public void updateCategory(Long ledgerId, Long categoryId) {
        ledgerMapper.updateLedgerCategory(ledgerId, categoryId);
    }

    public void updateMemo(Long ledgerId, String memo) {
        ledgerMapper.updateLedgerMemo(ledgerId, memo);
    }
}
