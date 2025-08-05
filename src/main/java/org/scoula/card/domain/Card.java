package org.scoula.card.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    private Long id;
    private Long userId;
    private String finCardNumber;
    private String backCode;
    private String bankName;
    private String cardName;
    private String cardMaskednum;
    private String cardMemberType;
    private String cardType;
    private Boolean isActive;
}
