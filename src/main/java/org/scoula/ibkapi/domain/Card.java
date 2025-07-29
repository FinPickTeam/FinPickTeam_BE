package org.scoula.ibkapi.domain;

import lombok.Data;

@Data
public class Card {
    private Long id;
    private Long userId;
    private String oapiCardAltrNo;
    private String backCode;
    private String bankName;
    private String cardName;
    private String cardMaskednum;
    private String cardMemberType; // SELF or FAMILY
    private String cardType;       // CREDIT or DEBIT
    private String createdAt;
}
