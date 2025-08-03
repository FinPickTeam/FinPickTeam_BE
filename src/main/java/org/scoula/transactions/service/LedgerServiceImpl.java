package org.scoula.transactions.service;

import lombok.RequiredArgsConstructor;
import org.scoula.transactions.domain.Ledger;
import org.scoula.transactions.dto.LedgerDetailDto;
import org.scoula.transactions.dto.LedgerDto;
import org.scoula.transactions.exception.LedgerNotFoundException;
import org.scoula.transactions.mapper.LedgerMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LedgerServiceImpl implements LedgerService {

    private final LedgerMapper ledgerMapper;

    @Override
    public List<LedgerDto> getLedgers(Long userId, String from, String to, String category) {
        List<Ledger> ledgers = ledgerMapper.findLedgers(userId, from, to, category);

        if (ledgers == null || ledgers.isEmpty()) {
            throw new LedgerNotFoundException();
        }

        return ledgers.stream().map(this::convertToDto).toList();
    }



    @Override
    public LedgerDetailDto getLedgerDetail(Long userId, Long ledgerId) {
        Ledger ledger = ledgerMapper.findLedgerDetail(userId, ledgerId);

        if (ledger == null) {
            throw new LedgerNotFoundException();
        }

        return convertToDetailDto(ledger);
    }

    private LedgerDto convertToDto(Ledger l) {
        LedgerDto dto = new LedgerDto();
        dto.setId(l.getId());
        dto.setSourceType(l.getSourceType());
        dto.setSourceName(l.getSourceName());
        dto.setAmount(l.getAmount());
        dto.setType(l.getType());
        dto.setCategory(l.getCategory());  // Mapper에서 조인된 category.label
        dto.setDate(l.getDate());
        dto.setMerchantName(l.getMerchantName());
        return dto;
    }


    private LedgerDetailDto convertToDetailDto(Ledger l) {
        LedgerDetailDto dto = new LedgerDetailDto();
        dto.setId(l.getId());
        dto.setUserId(l.getUserId());
        dto.setSourceType(l.getSourceType());
        dto.setSourceName(l.getSourceName());
        dto.setAmount(l.getAmount());
        dto.setType(l.getType());
        dto.setCategory(l.getCategory());
        dto.setMemo(l.getMemo());
        dto.setAnalysis(l.getAnalysis());
        dto.setDate(l.getDate());
        dto.setMerchantName(l.getMerchantName());
        dto.setPlace(l.getPlace());
        dto.setAccountId(l.getAccountId());
        dto.setCardId(l.getCardId());
        return dto;
    }
}
