package org.scoula.card.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CardRegisterResponseDto {
    private Long cardId;
    private String finCardNumber;
}
