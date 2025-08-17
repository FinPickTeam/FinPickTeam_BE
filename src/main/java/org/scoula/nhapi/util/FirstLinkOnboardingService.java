package org.scoula.nhapi.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.scoula.account.domain.Account;
import org.scoula.account.mapper.AccountMapper;
import org.scoula.card.domain.Card;
import org.scoula.card.mapper.CardMapper;
import org.scoula.nhapi.client.NHApiClient;
import org.scoula.transactions.service.CardTransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirstLinkOnboardingService {

    private final NHApiClient nhApiClient;
    private final AccountMapper accountMapper;
    private final CardMapper cardMapper;
    private final CardTransactionService cardTxService;

    private static final int MIN_DEPOSIT = 2, MAX_DEPOSIT = 3;
    private static final int MIN_SAVING  = 1, MAX_SAVING  = 2;
    private static final int MIN_CARDS   = 2, MAX_CARDS   = 3;

    @Transactional
    public void runOnceOnFirstLink(Long userId) {
        Random r = new Random(java.util.Objects.hash(userId, "first-pack"));

        int targetDeposit = MIN_DEPOSIT + r.nextInt(MAX_DEPOSIT - MIN_DEPOSIT + 1);
        int targetSaving  = MIN_SAVING  + r.nextInt(MAX_SAVING  - MIN_SAVING  + 1);
        int targetCards   = MIN_CARDS   + r.nextInt(MAX_CARDS   - MIN_CARDS   + 1);

        int curDeposit = accountMapper.countByUserAndType(userId, "DEPOSIT");
        int curSaving  = accountMapper.countByUserAndType(userId, "SAVING");
        int curCards   = cardMapper.countByUser(userId);

        int mkDeposit = Math.max(0, targetDeposit - curDeposit);
        int mkSaving  = Math.max(0, targetSaving  - curSaving);
        int mkCards   = Math.max(0, targetCards  - curCards);

        log.info("🎁 Onboarding (user={}): make DEPOSIT +{}, SAVING +{}, CARDS +{}", userId, mkDeposit, mkSaving, mkCards);

        for (int i = 0; i < mkDeposit; i++) createDeposit(userId);
        for (int i = 0; i < mkSaving;  i++) createSaving(userId);
        for (int i = 0; i < mkCards;   i++) createCard(userId);
    }

    private void createDeposit(Long userId) {
        var b = AccountBrandingUtil.pickDeposit(userId);
        String finAcno = nhApiClient.callCheckFinAccount("RG" + System.nanoTime(), "19900101").optString("FinAcno");

        BigDecimal balance = new BigDecimal(nhApiClient.callInquireBalance(finAcno).optString("Ldbl", "900000"));

        Account acc = Account.builder()
                .userId(userId)
                .pinAccountNumber(finAcno)
                .bankCode(b.bankCode())
                .accountNumber(b.accountNumber())
                .productName(b.productName())
                .accountType("DEPOSIT")
                .balance(balance)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        accountMapper.insert(acc);
        log.info("🌱 DEPOSIT created: {} / {}", b.bankCode(), b.productName());
    }

    private void createSaving(Long userId) {
        var b = AccountBrandingUtil.pickSaving(userId);
        String finAcno = nhApiClient.callCheckFinAccount("RG" + System.nanoTime(), "19900101").optString("FinAcno");

        Account acc = Account.builder()
                .userId(userId)
                .pinAccountNumber(finAcno)
                .bankCode(b.bankCode())
                .accountNumber(b.accountNumber())
                .productName(b.productName())
                .accountType("SAVING")
                .balance(BigDecimal.ZERO)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        accountMapper.insert(acc);
        log.info("🌱 SAVING created: {} / {}", b.bankCode(), b.productName());
    }

    private void createCard(Long userId) {
        var b = CardBrandingUtil.pickForUser(userId, true);
        JSONObject open = nhApiClient.callOpenFinCard("MOCK-" + System.nanoTime(), "19990101");
        String rgno = open.optString("Rgno");
        String finCard = nhApiClient.checkOpenFinCard(rgno, "19990101").optString("FinCard");

        Card card = Card.builder()
                .userId(userId)
                .finCardNumber(finCard)
                .backCode("00")
                .bankName(b.bankName())
                .cardName(b.cardName())
                .cardMaskednum(b.masked())
                .cardMemberType("SELF")
                .cardType(b.cardType())
                .isActive(true)
                .build();
        cardMapper.insertCard(card);
        
        log.info("🌱 CARD created: {} {}", b.bankName(), b.cardName());
    }
}
