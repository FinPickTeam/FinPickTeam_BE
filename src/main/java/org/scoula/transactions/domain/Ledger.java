package org.scoula.transactions.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.card.domain.Card;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ledger {
    private Long id;
    private Long userId;
    private Long sourceId;
    private Long accountId;
    private Long cardId;
    private String sourceType; // ACCOUNT or CARD
    private String sourceName;
    private String type; // INCOME or EXPENSE
    private BigDecimal amount;
    private String category; //조회 전용 필드
    private Integer categoryId;
    private String memo;
    private String analysis;
    private LocalDateTime date;
    private String merchantName;
    private String place;
    private LocalDateTime createdAt;

    // ✅ 계좌 거래 전용 생성자
    public Ledger(AccountTransaction tx, String sourceName, int categoryId) {
        this.userId = tx.getUserId();
        this.sourceId = tx.getId();
        this.accountId = tx.getAccountId();
        this.sourceType = "ACCOUNT";
        this.sourceName = sourceName;
        this.type = "INCOME";
        this.amount = tx.getAmount();
        this.categoryId = categoryId; // ✅ 고정된 "이체" categoryId 값
        this.memo = null;
        this.analysis = null;
        this.date = tx.getDate();
        this.place = tx.getPlace();
    }

    // ✅ 카드 거래 전용 생성자
    public static Ledger fromCardTransaction(CardTransaction tx, Card card) {
        String category = mapCategoryFromTpbcdNm(tx.getTpbcdNm());
        Long categoryId = mapCategoryIdFromName(category);
        return Ledger.builder()
                .userId(tx.getUserId())
                .sourceId(tx.getId())
                .cardId(tx.getCardId())
                .sourceType("CARD")
                .sourceName(card.getCardName()) // 💡 여기에 카드 이름
                .type("EXPENSE")
                .amount(tx.getAmount())
                .category(category)
                .categoryId(categoryId.intValue())
                .memo(null)
                .analysis(null)
                .date(tx.getApprovedAt())
                .merchantName(tx.getMerchantName())
                .place(tx.getMerchantName())
                .createdAt(LocalDateTime.now())
                .build();
    }


    public static String mapCategoryFromTpbcdNm(String tpbcdNm) {
        if (tpbcdNm == null) return "uncategorized";
        tpbcdNm = tpbcdNm.toLowerCase();

        if (tpbcdNm.matches(".*(식당|한식|중식|양식|일식|분식|고기|치킨|족발).*")) return "food";
        if (tpbcdNm.matches(".*(커피|카페|베이커리|제과|디저트|아이스크림|간식).*")) return "cafe";
        if (tpbcdNm.matches(".*(의류|화장품|미용|잡화|백화점|패션|뷰티).*")) return "shopping";
        if (tpbcdNm.matches(".*(편의점|마트|슈퍼|잡화점|할인점|생활용품).*")) return "mart";
        if (tpbcdNm.matches(".*(통신|전기|수도|가스|관리비|주거|인터넷).*")) return "house";
        if (tpbcdNm.matches(".*(영화|서점|공연|게임|문화|레저|넷플릭스|멜론).*")) return "hobby";
        if (tpbcdNm.matches(".*(주유소|교통|버스|지하철|택시|고속도로|톨게이트|자동차).*")) return "transport";
        if (tpbcdNm.matches(".*(보험|증권|금융|적금|연금|수수료|이자).*")) return "finance";
        if (tpbcdNm.matches(".*(유튜브|넷플릭스|멜론|정기결제|구독).*")) return "subscription";
        if (tpbcdNm.matches(".*(이체|송금|계좌이체|자동이체).*")) return "transfer";
        if (tpbcdNm.matches(".*(기부|선물|축의금|장례식|결혼|반려동물).*")) return "etc";

        return "uncategorized";
    }

    private static final Map<String, Long> CATEGORY_ID_MAP = Map.ofEntries(
            Map.entry("food", 1L),
            Map.entry("cafe", 2L),
            Map.entry("shopping", 3L),
            Map.entry("mart", 4L),
            Map.entry("house", 5L),
            Map.entry("hobby", 6L),
            Map.entry("transport", 7L),
            Map.entry("finance", 8L),
            Map.entry("subscription", 9L),
            Map.entry("transfer", 10L),
            Map.entry("etc", 11L),
            Map.entry("uncategorized", 12L)
    );

    public static Long mapCategoryIdFromName(String category) {
        return CATEGORY_ID_MAP.getOrDefault(category, 12L); // uncategorized 기본값
    }


}

