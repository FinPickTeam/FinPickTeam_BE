package org.scoula.card.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.scoula.card.domain.Card;
import org.scoula.card.dto.CardRegisterResponseDto;
import org.scoula.common.exception.BaseException;
import org.scoula.common.exception.ForbiddenException;
import org.scoula.nhapi.dto.FinCardRequestDto;
import org.scoula.card.mapper.CardMapper;
import org.scoula.nhapi.client.NHApiClient;
import org.scoula.transactions.service.CardTransactionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final NHApiClient nhApiClient;
    private final CardMapper cardMapper;
    private final CardTransactionService cardTransactionService;

    @Override
    public CardRegisterResponseDto registerCard(FinCardRequestDto dto) {
        // 🔹 1단계: 핀카드 발급
        JSONObject res1 = nhApiClient.callOpenFinCard(dto.getCardNumber(), dto.getBirthday());
        log.info("📦 핀카드 발급 응답: {}", res1.toString());

        if (!res1.has("Rgno")) {
            throw new BaseException("핀카드 발급 실패: 'Rgno' 없음. 응답 = " + res1.toString(), 500);
        }

        String rgno = res1.getString("Rgno");


        // 🔹 2단계: 핀카드 확인
        JSONObject res2 = nhApiClient.checkOpenFinCard(rgno, dto.getBirthday());
        String finCardNumber = res2.optString("FinCard");

        // 🔹 3단계: 카드 DB 등록
        Card card = Card.builder()
                .userId(1L) // 하드코딩
                .finCardNumber(finCardNumber)
                .backCode("00")
                .bankName("KB국민")
                .cardName("IT's Your Life 카드")
                .cardMaskednum("7018-****-****-1234")
                .cardMemberType("SELF")
                .cardType("DEBIT")
                .build();
        cardMapper.insertCard(card);

        // 🔹 4단계: 승인내역 동기화 (초기 전체 동기화)
        cardTransactionService.syncCardTransactions(1L, card.getId(), finCardNumber, true);

        log.info("✅ 카드 등록 및 승인내역 동기화 완료: {}", card);

        return CardRegisterResponseDto.builder()
                .cardId(card.getId())
                .finCardNumber(finCardNumber)
                .build();
    }

    @Override
    public void syncCardById(Long cardId) {
        Card card = cardMapper.findById(cardId);
        if (card == null) throw new BaseException("해당 카드가 존재하지 않습니다.", 404);

        cardTransactionService.syncCardTransactions(
                card.getUserId(),
                card.getId(),
                card.getFinCardNumber(),
                true
        );

        log.info("✅ 카드 {} 승인내역 갱신 완료", cardId);
    }

    @Override
    public void syncAllCardsByUserId(Long userId) {
        List<Card> cards = cardMapper.findByUserId(userId);
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

}
