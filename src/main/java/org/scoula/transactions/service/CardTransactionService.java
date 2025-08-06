package org.scoula.transactions.service;

import org.scoula.transactions.dto.CardTransactionDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CardTransactionService {
    List<CardTransactionDto> getCardTransactions(Long userId, Long cardId, String from, String to);
    void syncCardTransactions(Long userId, Long cardId, String finCard, boolean isInitial);
}
