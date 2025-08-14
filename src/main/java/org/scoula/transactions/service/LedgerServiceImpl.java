package org.scoula.transactions.service;

import lombok.RequiredArgsConstructor;
import org.scoula.account.domain.Account;
import org.scoula.account.mapper.AccountMapper;
import org.scoula.card.domain.Card;
import org.scoula.card.mapper.CardMapper;
import org.scoula.common.exception.BaseException;
import org.scoula.transactions.domain.Ledger;
import org.scoula.transactions.dto.AnalysisResult;
import org.scoula.transactions.dto.LedgerDetailDto;
import org.scoula.transactions.dto.LedgerDto;
import org.scoula.transactions.exception.LedgerNotFoundException;
import org.scoula.transactions.mapper.LedgerMapper;
import org.scoula.transactions.util.AnalysisEngine;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LedgerServiceImpl implements LedgerService {

    private final LedgerMapper ledgerMapper;
    private final AccountMapper accountMapper;
    private final CardMapper cardMapper;
    private final AnalysisEngine analysisEngine;


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
        if (ledger == null) throw new LedgerNotFoundException();

        if (ledger.getAccountId() != null) {
            Account acc = accountMapper.findById(ledger.getAccountId());
            if (!Boolean.TRUE.equals(acc.getIsActive())) {
                throw new BaseException("비활성화된 계좌입니다.", 400);
            }
        }
        if (ledger.getCardId() != null) {
            Card card = cardMapper.findById(ledger.getCardId());
            if (!Boolean.TRUE.equals(card.getIsActive())) {
                throw new BaseException("비활성화된 카드입니다.", 400);
            }
        }

        // 월 범위 [월초, 다음달 1일) + EXPENSE만 조회하는 쿼리 사용 권장
        var ym = java.time.YearMonth.from(ledger.getDate());
        var monthStart = ym.atDay(1).atStartOfDay();
        var nextMonthStart = ym.plusMonths(1).atDay(1).atStartOfDay();

        List<Ledger> monthLedgers = ledgerMapper.selectUserLedgersBetween(
                userId, monthStart, nextMonthStart // 이 쿼리에서 AND l.type='EXPENSE'
        );


        // analysis가 null이면, 정교한 분석!
        String analysisText = ledger.getAnalysis();
        if ((analysisText == null || analysisText.isEmpty())
                && "EXPENSE".equalsIgnoreCase(ledger.getType())) {

            AnalysisResult ar = analysisEngine.analyze(ledger, monthLedgers);

            // 결과 메시지가 비었으면 저장 스킵
            if (ar != null && ar.getMessage() != null && !ar.getMessage().isBlank()) {
                String onlyMessage = ar.getMessage(); // ◀︎ 태그 없이 “메시지만” 저장
                ledgerMapper.updateAnalysis(ledger.getId(), onlyMessage);
                analysisText = onlyMessage;
            }
        }

        LedgerDetailDto dto = convertToDetailDto(ledger);
        dto.setAnalysis(stripTag(analysisText));
        return dto;
    }

    private String stripTag(String s) {
        if (s == null) return null;
        return s.replaceFirst("^\\[[^\\]]+\\]\\s*", "");
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
