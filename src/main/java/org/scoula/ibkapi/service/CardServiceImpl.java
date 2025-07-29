package org.scoula.ibkapi.service;

import lombok.RequiredArgsConstructor;
import org.scoula.ibkapi.exception.CardErrorCode;
import org.scoula.ibkapi.exception.CardException;
import org.scoula.ibkapi.dto.CardDto;
import org.scoula.ibkapi.dto.CardTransactionDto;
import org.scoula.ibkapi.mapper.CardMapper;
import org.scoula.ibkapi.util.IBKCardApiClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardMapper cardMapper;
    private final IBKCardApiClient ibkCardApiClient;

    @Override
    public void syncCardList(Long userId) {
        String nextKey = null;

        do {
            List<CardDto> cards = ibkCardApiClient.callCardList(nextKey);

            if (cards == null) {
                throw new CardException(CardErrorCode.API_CALL_FAILED);
            }

            for (CardDto card : cards) {
                card.setUserId(userId);

                if (cardMapper.findCardByAltrNo(card.getOapiCardAltrNo()) == null) {
                    cardMapper.insertCard(card);
                }
                // else {
                //     throw new CardException(CardErrorCode.DUPLICATE_CARD); // 필요 시 중복에러로
                // }
            }

            nextKey = cards.size() == 10 ? cards.get(cards.size() - 1).getCardMaskednum() : null;

        } while (nextKey != null);
    }

    @Override
    public void syncCardTransactions(Long userId) {
        List<CardDto> cards = cardMapper.findCardsByUserId(userId);

        if (cards == null || cards.isEmpty()) {
            throw new CardException(CardErrorCode.CARD_NOT_FOUND);
        }

        for (CardDto card : cards) {
            String nextApn = null;

            do {
                List<CardTransactionDto> transactions =
                        ibkCardApiClient.callTransactionList(card.getOapiCardAltrNo(), nextApn);

                if (transactions == null) {
                    throw new CardException(CardErrorCode.API_CALL_FAILED);
                }

                for (CardTransactionDto tx : transactions) {
                    tx.setUserId(userId);
                    tx.setOapiCardAltrNo(card.getOapiCardAltrNo());

                    if (cardMapper.findTransactionByAuthNumber(tx.getAuthNumber()) == null) {
                        cardMapper.insertCardTransaction(tx);
                    }
                    // else {
                    //     throw new CardException(CardErrorCode.DUPLICATE_TRANSACTION); // 필요 시
                    // }
                }

                nextApn = (transactions.size() == 15)
                        ? transactions.get(transactions.size() - 1).getAuthNumber()
                        : null;

            } while (nextApn != null);
        }
    }
}
