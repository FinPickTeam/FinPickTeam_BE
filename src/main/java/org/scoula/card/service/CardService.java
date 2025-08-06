package org.scoula.card.service;

import org.scoula.card.dto.CardDto;
import org.scoula.card.dto.CardRegisterResponseDto;
import org.scoula.nhapi.dto.FinCardRequestDto;

import java.util.List;

public interface CardService {
    CardRegisterResponseDto registerCard(FinCardRequestDto dto);
    void syncCardById(Long cardId);
    void syncAllCardsByUserId(Long userId);
    void deactivateCard(Long cardId, Long userId);
    List<CardDto> getActiveCards(Long userId);
}
