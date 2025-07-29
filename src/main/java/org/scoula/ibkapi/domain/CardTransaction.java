package org.scoula.ibkapi.domain;

import lombok.Data;

@Data
public class CardTransaction {
    private Long id;
    private Long userId;
    private String oapiCardAltrNo;

    private String authNumber;
    private String approvedAt;

    private Double amount;
    private Boolean isCancelled;
    private Double cancelAmount;
    private String cancelledAt;

    private String merchantName;
    private String merchantIndustryCode;
    private String merchantIndustry;

    private Integer installmentMonth;
    private String currency;
    private Double foreignAmount;

    private String purchaseDate;
    private String createdAt;
}

