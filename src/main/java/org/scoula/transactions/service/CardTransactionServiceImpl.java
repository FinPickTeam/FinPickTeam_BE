package org.scoula.transactions.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.card.domain.Card;
import org.scoula.card.mapper.CardMapper;
import org.scoula.common.exception.BaseException;
import org.scoula.nhapi.dto.NhCardTransactionResponseDto;
import org.scoula.nhapi.service.NhCardService;
import org.scoula.transactions.domain.CardTransaction;
import org.scoula.transactions.domain.Ledger;
import org.scoula.transactions.mapper.CardTransactionMapper;
import org.scoula.transactions.mapper.LedgerMapper;
import org.scoula.transactions.exception.CardTransactionNotFoundException;
import org.scoula.transactions.dto.CardTransactionDto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardTransactionServiceImpl implements CardTransactionService {

    private final NhCardService nhCardService;
    private final CardTransactionMapper mapper;
    private final LedgerMapper ledgerMapper;
    private final CardMapper cardMapper;

    @Override
    public List<CardTransactionDto> getCardTransactions(Long userId, Long cardId, String from, String to) {
        Card card = cardMapper.findById(cardId);
        if (card == null) throw new BaseException("해당 카드가 존재하지 않습니다.", 404);
        if (!Boolean.TRUE.equals(card.getIsActive())) {
            throw new BaseException("비활성화된 카드입니다.", 400);
        }

        List<CardTransaction> txList = mapper.findCardTransactions(userId, cardId, from, to);
        if (txList == null || txList.isEmpty()) {
            throw new CardTransactionNotFoundException();
        }

        return txList.stream().map(this::toDto).toList();
    }

    private CardTransactionDto toDto(CardTransaction tx) {
        CardTransactionDto dto = new CardTransactionDto();
        dto.setId(tx.getId());
        dto.setUserId(tx.getUserId());
        dto.setCardId(tx.getCardId());
        dto.setAuthNumber(tx.getAuthNumber());
        dto.setSalesType(tx.getSalesType());
        dto.setApprovedAt(tx.getApprovedAt());
        dto.setAmount(tx.getAmount());
        dto.setMerchantName(tx.getMerchantName());
        dto.setTpbcdNm(tx.getTpbcdNm());
        dto.setIsCancelled(tx.getIsCancelled());
        return dto;
    }

    // ✅ 신규 메서드: 초기 여부 구분 포함 동기화
    @Override
    public void syncCardTransactions(Long userId, Long cardId, String finCard, boolean isInitial) {
        LocalDate to = LocalDate.now();
        LocalDate from = isInitial ? to.minusMonths(3) : getLastSyncDate(cardId, to);

        List<NhCardTransactionResponseDto> dtoList = nhCardService.callCardTransactionList(
                userId, finCard,
                from.format(DateTimeFormatter.BASIC_ISO_DATE),
                to.format(DateTimeFormatter.BASIC_ISO_DATE)
        );

        Card card = cardMapper.findById(cardId);
        if (card == null) throw new BaseException("카드 정보가 존재하지 않습니다.", 404);

        for (NhCardTransactionResponseDto dto : dtoList) {
            if (mapper.existsByUserIdAndKey(userId, dto.getAuthNumber(), dto.getApprovedAt())) continue;

            CardTransaction tx = new CardTransaction(dto, userId, cardId);
            mapper.insert(tx);

            if (!tx.getIsCancelled()) {
                Ledger ledger = Ledger.fromCardTransaction(tx, card); // ✅ 카드 정보 포함
                ledgerMapper.cardInsert(ledger);
            }
        }


        log.info("✅ 카드 {} 승인내역 동기화 완료 ({}건)", cardId, dtoList.size());
    }

    // ✅ 마지막 승인일 기준 동기화 시작일 결정
    private LocalDate getLastSyncDate(Long cardId, LocalDate fallback) {
        LocalDateTime last = mapper.findLastTransactionDate(cardId);
        return last != null ? last.toLocalDate().plusDays(1) : fallback.minusMonths(3);
    }
}
