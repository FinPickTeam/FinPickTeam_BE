package org.scoula.transactions.service;

import lombok.RequiredArgsConstructor;
import org.scoula.transactions.domain.CardTransaction;
import org.scoula.transactions.dto.CardTransactionDto;
import org.scoula.transactions.exception.CardTransactionNotFoundException;
import org.scoula.transactions.mapper.CardTransactionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardTransactionServiceImpl implements CardTransactionService {

    private final CardTransactionMapper mapper;

    @Override
    public List<CardTransactionDto> getCardTransactions(Long userId, Long cardId, String from, String to) {
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
        dto.setTpbcd(tx.getTpbcd());
        dto.setTpbcdNm(tx.getTpbcdNm());
        return dto;
    }
}

