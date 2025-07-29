package org.scoula.ibkapi.dto;

import lombok.Data;

@Data
public class CardDto {
    private Long userId;
    private String oapiCardAltrNo;
    private String backCode;
    private String bankName;
    private String cardName;
    private String cardMaskednum;
    private String cardMemberType;  // "SELF" or "FAMILY"
    private String cardType;        // "CREDIT" or "DEBIT"
}
