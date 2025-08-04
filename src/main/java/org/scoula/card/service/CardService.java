package org.scoula.card.service;

import org.scoula.card.dto.CardRegisterResponseDto;
import org.scoula.nhapi.dto.FinCardRequestDto;

public interface CardService {
    CardRegisterResponseDto registerCard(FinCardRequestDto dto);
    void syncCardById(Long cardId);
    void syncAllCardsByUserId(Long userId);
}
