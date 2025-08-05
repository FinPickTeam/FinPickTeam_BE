package org.scoula.card.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.card.domain.Card;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDto {

    private Long id;
    private String cardName;
    private String cardMaskednum;
    private String bankName;
    private String cardType;

    public static CardDto from(Card card) {
        return CardDto.builder()
                .id(card.getId())
                .cardName(card.getCardName())
                .cardMaskednum(card.getCardMaskednum())
                .bankName(card.getBankName())
                .cardType(card.getCardType())
                .build();
    }
}
