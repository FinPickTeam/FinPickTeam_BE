package org.scoula.card.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.scoula.card.domain.Card;
import org.scoula.card.dto.CardDto;
import org.scoula.card.dto.CardRegisterResponseDto;
import org.scoula.common.exception.BaseException;
import org.scoula.common.exception.ForbiddenException;
import org.scoula.nhapi.dto.FinCardRequestDto;
import org.scoula.card.mapper.CardMapper;
import org.scoula.transactions.mapper.CardTransactionMapper;
import org.scoula.nhapi.client.NHApiClient;
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

    @Override
    public CardRegisterResponseDto registerCard(Long userId, FinCardRequestDto dto) {
        // 1. 핀카드 발급
        JSONObject res1 = nhApiClient.callOpenFinCard(dto.getCardNumber(), dto.getBirthday());
        log.info("📦 핀카드 발급 응답: {}", res1.toString());

        if (!res1.has("Rgno")) {
            throw new BaseException("핀카드 발급 실패: 'Rgno' 없음. 응답 = " + res1.toString(), 500);
        }

        String rgno = res1.getString("Rgno");

        // 2. 핀카드 확인
        JSONObject res2 = nhApiClient.checkOpenFinCard(rgno, dto.getBirthday());
        String finCardNumber = res2.optString("FinCard");

        // 3. 카드 DB 등록
        Card card = Card.builder()
                .userId(userId)
                .finCardNumber(finCardNumber)
                .backCode("00")
                .bankName("KB국민")
                .cardName("IT's Your Life 카드")
                .cardMaskednum("7018-****-****-1234")
                .cardMemberType("SELF")
                .cardType("DEBIT")
                .build();
        cardMapper.insertCard(card);

        // 4. 승인내역 동기화 (초기 전체 동기화)
        cardTransactionService.syncCardTransactions(userId, card.getId(), finCardNumber, true);

        log.info("✅ 카드 등록 및 승인내역 동기화 완료: {}", card);

        return CardRegisterResponseDto.builder()
                .cardId(card.getId())
                .finCardNumber(finCardNumber)
                .build();
    }

    @Override
    public void syncCardById(Long userId, Long cardId) {
        Card card = cardMapper.findById(cardId);
        if (card == null) {
            throw new BaseException("해당 카드가 존재하지 않습니다.", 404);
        }
        if (!card.getUserId().equals(userId)) {
            throw new ForbiddenException("본인 카드만 동기화할 수 있습니다");
        }
        if (!Boolean.TRUE.equals(card.getIsActive())) {
            throw new BaseException("비활성화된 카드입니다.", 400);
        }

        cardTransactionService.syncCardTransactions(
                userId,
                card.getId(),
                card.getFinCardNumber(),
                true
        );

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
            cardTransactionService.syncCardTransactions(
                    userId,
                    card.getId(),
                    card.getFinCardNumber(),
                    true
            );
            log.info("✅ 카드 동기화 완료: cardId={}, userId={}", card.getId(), userId);
        }
    }

    @Override
    public void deactivateCard(Long cardId, Long userId) {
        Card card = cardMapper.findById(cardId);

        if (card == null || !card.getUserId().equals(userId)) {
            throw new ForbiddenException("본인 카드만 삭제할 수 있습니다");
        }

        if (!Boolean.TRUE.equals(card.getIsActive())) {
            return;
        }

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
}
