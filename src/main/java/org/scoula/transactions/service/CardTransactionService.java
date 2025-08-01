package org.scoula.transactions.service;

import org.scoula.transactions.dto.CardTransactionDto;

import java.util.List;

public interface CardTransactionService {
    List<CardTransactionDto> getCardTransactions(Long userId, Long cardId);
}
