package org.scoula.transactions.service;

import org.scoula.transactions.dto.LedgerDetailDto;
import org.scoula.transactions.dto.LedgerDto;

import java.util.List;

public interface LedgerService {
    List<LedgerDto> getLedgers(Long userId, String from, String to, String category);
    LedgerDetailDto getLedgerDetail(Long userId, Long ledgerId);
}
