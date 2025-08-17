package org.scoula.card.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.scoula.card.domain.Card;
import org.scoula.card.dto.CardDto;
import org.scoula.card.dto.CardRegisterResponseDto;
import org.scoula.card.mapper.CardMapper;
import org.scoula.common.exception.BaseException;
import org.scoula.common.exception.ForbiddenException;
import org.scoula.nhapi.client.NHApiClient;
import org.scoula.nhapi.dto.FinCardRequestDto;
import org.scoula.nhapi.util.FirstLinkOnboardingService;
import org.scoula.nhapi.util.MaskingUtil;
import org.scoula.transactions.mapper.CardTransactionMapper;
import org.scoula.transactions.service.CardTransactionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final NHApiClient nhApiClient;
    private final CardMapper cardMapper;
    private final CardTransactionMapper cardTransactionMapper;
    private final CardTransactionService cardTransactionService;
    private final FirstLinkOnboardingService firstLinkOnboardingService;

    @Override
    public CardRegisterResponseDto registerCard(Long userId, FinCardRequestDto dto) {

        // ① DTO 비거나 placeholder면 → MOCK 경로
        if (isEmptyOrPlaceholder(dto)) {
            var brand = org.scoula.nhapi.util.CardBrandingUtil.pickForUser(userId, true); // 신용

            JSONObject res1 = nhApiClient.callOpenFinCard("MOCK-" + System.nanoTime(), "19990101");
            String rgno = res1.optString("Rgno");
            String finCardNumber = nhApiClient.checkOpenFinCard(rgno, "19990101").optString("FinCard");

            // 브랜드의 masked 값 대신 카드 규격 마스킹(앞4/뒤4만 노출)
            String masked = MaskingUtil.maskCard(finCardNumber);

            Card card = Card.builder()
                    .userId(userId)
                    .finCardNumber(finCardNumber)
                    .backCode("00")
                    .bankName(brand.bankName())
                    .cardName(brand.cardName())
                    .cardMaskednum(masked)        // 마스킹 저장
                    .cardMemberType("SELF")
                    .cardType(brand.cardType())   // "CREDIT"
                    .isActive(true)
                    .build();
            cardMapper.insertCard(card);

            // 첫 연동 패키지
            firstLinkOnboardingService.runOnceOnFirstLink(userId);

            log.info("✅ [MOCK] 카드 등록 완료: {}", card);
            return CardRegisterResponseDto.builder()
                    .cardId(card.getId())
                    .finCardNumber(finCardNumber)
                    .build();
        }

        // ② 정상 경로
        JSONObject res1 = nhApiClient.callOpenFinCard(dto.getCardNumber(), dto.getBirthday());
        log.info("📦 핀카드 발급 응답: {}", res1);
        if (!res1.has("Rgno")) throw new BaseException("핀카드 발급 실패: 'Rgno' 없음. 응답 = " + res1, 500);

        String rgno = res1.getString("Rgno");
        String finCardNumber = nhApiClient.checkOpenFinCard(rgno, dto.getBirthday()).optString("FinCard");

        Card card = Card.builder()
                .userId(userId)
                .finCardNumber(finCardNumber)
                .backCode("00")
                .bankName("KB국민")
                .cardName("IT's Your Life 카드")
                .cardMaskednum(MaskingUtil.maskCard(finCardNumber)) // 가운데 마스킹
                .cardMemberType("SELF")
                .cardType("DEBIT")
                .isActive(true)
                .build();
        cardMapper.insertCard(card);

        cardTransactionService.syncCardTransactions(userId, card.getId(), finCardNumber, true);
        firstLinkOnboardingService.runOnceOnFirstLink(userId);

        log.info("✅ 카드 등록 및 승인내역 동기화 완료: {}", card);
        return CardRegisterResponseDto.builder()
                .cardId(card.getId())
                .finCardNumber(finCardNumber)
                .build();
    }

    @Override
    public void syncCardById(Long userId, Long cardId) {
        Card card = cardMapper.findById(cardId);
        if (card == null) throw new BaseException("해당 카드가 존재하지 않습니다.", 404);
        if (!card.getUserId().equals(userId)) throw new ForbiddenException("본인 카드만 동기화할 수 있습니다");
        if (!Boolean.TRUE.equals(card.getIsActive())) throw new BaseException("비활성화된 카드입니다.", 400);

        cardTransactionService.syncCardTransactions(userId, card.getId(), card.getFinCardNumber(), true);
        log.info("✅ 카드 {} 승인내역 갱신 완료", cardId);
    }

    @Override
    public void syncAllCardsByUserId(Long userId) {
        List<Card> cards = cardMapper.findActiveByUserId(userId);
        if (cards == null || cards.isEmpty()) {
            log.info("❗️ 동기화할 카드가 없습니다. userId={}", userId);
            return;
        }
        for (Card card : cards) {
            cardTransactionService.syncCardTransactions(userId, card.getId(), card.getFinCardNumber(), true);
            log.info("✅ 카드 동기화 완료: cardId={}, userId={}", card.getId(), userId);
        }
    }

    @Override
    public void deactivateCard(Long cardId, Long userId) {
        Card card = cardMapper.findById(cardId);
        if (card == null || !card.getUserId().equals(userId)) throw new ForbiddenException("본인 카드만 삭제할 수 있습니다");
        if (!Boolean.TRUE.equals(card.getIsActive())) return;
        cardMapper.updateIsActive(cardId, false);
    }

    @Override
    public List<CardDto> getCardsWithMonth(Long userId, YearMonth month) {
        List<Card> cards = cardMapper.findActiveByUserId(userId);
        String start = month.atDay(1).toString();
        String end = month.atEndOfMonth().toString();
        return cards.stream().map(card -> {
            BigDecimal spent = cardTransactionMapper.sumMonthlySpendingByCard(userId, card.getId(), start, end);
            return CardDto.from(card, spent == null ? BigDecimal.ZERO : spent);
        }).collect(Collectors.toList());
    }

    /* ===== 헬퍼 ===== */
    private static boolean isEmptyOrPlaceholder(FinCardRequestDto dto) {
        return dto == null
                || blankLike(dto.getCardNumber())
                || blankLike(dto.getBirthday());
    }
    private static boolean blankLike(String s) {
        if (s == null) return true;
        String v = s.trim();
        return v.isEmpty()
                || v.equalsIgnoreCase("string")
                || v.equalsIgnoreCase("null")
                || v.equalsIgnoreCase("undefined");
    }
}
