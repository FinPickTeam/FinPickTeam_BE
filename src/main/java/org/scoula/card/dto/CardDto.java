package org.scoula.card.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.card.domain.Card;

import java.math.BigDecimal;

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
    private BigDecimal monthlySpent;

    public static CardDto from(Card card, BigDecimal monthlySpent) {
        return CardDto.builder()
                .id(card.getId())
                .cardName(card.getCardName())
                .cardMaskednum(card.getCardMaskednum())
                .bankName(card.getBankName())
                .cardType(card.getCardType())
                .monthlySpent(monthlySpent)
                .build();
    }
}
