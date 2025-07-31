package org.scoula.transactions.service;

import org.scoula.transactions.dto.LedgerDetailDto;
import org.scoula.transactions.dto.LedgerDto;

import java.util.List;

public interface LedgerService {
    List<LedgerDto> getLedgerByUserId(Long userId);
    LedgerDetailDto getLedgerDetail(Long userId, Long ledgerId);

}
