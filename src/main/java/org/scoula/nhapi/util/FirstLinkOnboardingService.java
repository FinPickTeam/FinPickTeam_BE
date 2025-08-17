package org.scoula.nhapi.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.scoula.account.domain.Account;
import org.scoula.account.mapper.AccountMapper;
import org.scoula.card.domain.Card;
import org.scoula.card.mapper.CardMapper;
import org.scoula.nhapi.client.NHApiClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirstLinkOnboardingService {

    private final NHApiClient nhApiClient;
    private final AccountMapper accountMapper;
    private final CardMapper cardMapper;
    // 온보딩에선 tx sync 호출 안 함

    private static final int MIN_DEPOSIT = 2, MAX_DEPOSIT = 3;
    private static final int MIN_SAVING  = 1, MAX_SAVING  = 2;
    private static final int MIN_CARDS   = 2, MAX_CARDS   = 3;
    private static final int MAX_RETRY   = 20;

    @Transactional
    public void runOnceOnFirstLink(Long userId) {
        Random r = new Random(Objects.hash(userId, "first-pack"));

        int targetDeposit = MIN_DEPOSIT + r.nextInt(MAX_DEPOSIT - MIN_DEPOSIT + 1);
        int targetSaving  = MIN_SAVING  + r.nextInt(MAX_SAVING  - MIN_SAVING  + 1);
        int targetCards   = MIN_CARDS   + r.nextInt(MAX_CARDS   - MIN_CARDS   + 1);

        int curDeposit = accountMapper.countByUserAndType(userId, "DEPOSIT");
        int curSaving  = accountMapper.countByUserAndType(userId, "SAVING");
        int curCards   = cardMapper.countByUser(userId);

        int mkDeposit = Math.max(0, targetDeposit - curDeposit);
        int mkSaving  = Math.max(0, targetSaving  - curSaving);
        int mkCards   = Math.max(0, targetCards  - curCards);

        log.info("🎁 Onboarding (user={}): make DEPOSIT +{}, SAVING +{}, CARDS +{}",
                userId, mkDeposit, mkSaving, mkCards);

        /* ====== 0) DB에 이미 존재하는 이름들을 선(先)로딩해서 중복 베이스라인으로 사용 ====== */
        // 계좌 상품명(타입 무관, 전체) / 카드명
        Set<String> existingAccountNames = new HashSet<>(safeList(accountMapper.findProductNamesByUser(userId)));
        Set<String> existingCardNames    = new HashSet<>(safeList(cardMapper.findCardNamesByUser(userId)));

        /* ====== 1) DEPOSIT: 서로 다른 상품명 보장 + 가운데 마스킹 번호 ====== */
        Set<String> usedAccountNames = new HashSet<>(existingAccountNames); // 실행 중 중복 방지
        for (int i = 0; i < mkDeposit; i++) {

            // 브랜드 고르기(중복 이름 피해서 MAX_RETRY 번 시도)
            AccountBrand b = null;
            for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
                var cand = AccountBrandingUtil.pickDeposit(userId + attempt + (i * 131));
                if (isAccountNameTaken(userId, cand.productName(), usedAccountNames)) continue;
                b = new AccountBrand(cand.bankCode(), cand.productName());
                break;
            }
            if (b == null) {
                log.warn("⏭️ DEPOSIT 생성 스킵(중복 회피 실패)");
                continue;
            }
            usedAccountNames.add(b.productName());

            String finAcno = nhApiClient
                    .callCheckFinAccount("RG" + System.nanoTime(), "19900101")
                    .optString("FinAcno");

            // 계좌번호 생성 → 가운데 마스킹 저장
            String accountNumber = null;
            BigDecimal balance = readBalanceSafe(finAcno);

            // 유니크 충돌 시 재생성(최대 MAX_RETRY)
            for (int retry = 0; retry < MAX_RETRY; retry++) {
                String rawAccNo = generateAccountNumber(b.bankCode(), userId, i + retry);
                String displayAccNo = MaskingUtil.maskAccount(rawAccNo);

                Account acc = Account.builder()
                        .userId(userId)
                        .pinAccountNumber(finAcno)
                        .bankCode(b.bankCode())
                        .accountNumber(displayAccNo)      // 마스킹된 표시용 번호 저장
                        .productName(b.productName())
                        .accountType("DEPOSIT")
                        .balance(balance)
                        .isActive(true)
                        .createdAt(LocalDateTime.now())
                        .build();
                try {
                    accountMapper.insert(acc);
                    accountNumber = acc.getAccountNumber();
                    log.info("🌱 DEPOSIT created: {} / {} / {}", b.bankCode(), b.productName(), accountNumber);
                    break;
                } catch (Exception dup) {
                    if (retry == MAX_RETRY - 1) throw dup;
                }
            }
        }

        /* ====== 2) SAVING: 서로 다른 상품명 보장 + 가운데 마스킹 번호 ====== */
        for (int i = 0; i < mkSaving; i++) {

            AccountBrand b = null;
            for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
                var cand = AccountBrandingUtil.pickSaving(userId + attempt + (i * 97));
                if (isAccountNameTaken(userId, cand.productName(), usedAccountNames)) continue;
                b = new AccountBrand(cand.bankCode(), cand.productName());
                break;
            }
            if (b == null) {
                log.warn("⏭️ SAVING 생성 스킵(중복 회피 실패)");
                continue;
            }
            usedAccountNames.add(b.productName());

            String finAcno = nhApiClient
                    .callCheckFinAccount("RG" + System.nanoTime(), "19900101")
                    .optString("FinAcno");

            // 번호 생성/마스킹
            for (int retry = 0; retry < MAX_RETRY; retry++) {
                String rawSavingNo = generateAccountNumber(b.bankCode(), userId, 100 + i + retry);
                String displaySavingNo = MaskingUtil.maskAccount(rawSavingNo);

                Account acc = Account.builder()
                        .userId(userId)
                        .pinAccountNumber(finAcno)
                        .bankCode(b.bankCode())
                        .accountNumber(displaySavingNo)
                        .productName(b.productName())
                        .accountType("SAVING")
                        .balance(BigDecimal.ZERO)
                        .isActive(true)
                        .createdAt(LocalDateTime.now())
                        .build();
                try {
                    accountMapper.insert(acc);
                    log.info("🌱 SAVING created: {} / {} / {}", b.bankCode(), b.productName(), displaySavingNo);
                    break;
                } catch (Exception dup) {
                    if (retry == MAX_RETRY - 1) throw dup;
                }
            }
        }

        /* ====== 3) CARD: 서로 다른 카드명 보장 + 가운데 마스킹 번호(1234-****-****-5678) ====== */
        Set<String> usedCardNames = new HashSet<>(existingCardNames);
        for (int i = 0; i < mkCards; i++) {

            CardBrand b = null;
            for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
                var cand = CardBrandingUtil.pickForUser(userId + attempt + (i * 53), true);
                if (isCardNameTaken(userId, cand.cardName(), usedCardNames)) continue;
                b = new CardBrand(cand.bankName(), cand.cardName(), cand.cardType());
                break;
            }
            if (b == null) {
                log.warn("⏭️ CARD 생성 스킵(중복 회피 실패)");
                continue;
            }
            usedCardNames.add(b.cardName());

            JSONObject open = nhApiClient.callOpenFinCard("MOCK-" + System.nanoTime(), "19990101");
            String rgno = open.optString("Rgno");
            String finCard = nhApiClient.checkOpenFinCard(rgno, "19990101").optString("FinCard");

            String masked = generateMaskedCardNumber(userId, i); // 1234-****-****-5678

            Card card = Card.builder()
                    .userId(userId)
                    .finCardNumber(finCard)
                    .backCode("00")
                    .bankName(b.bankName())
                    .cardName(b.cardName())
                    .cardMaskednum(masked)
                    .cardMemberType("SELF")
                    .cardType(b.cardType())
                    .isActive(true)
                    .build();
            cardMapper.insertCard(card);
            log.info("🌱 CARD created: {} {} / {}", b.bankName(), b.cardName(), masked);
        }
    }

    /* ===== Helpers ===== */

    private static <T> List<T> safeList(List<T> in) {
        return in == null ? List.of() : in;
    }

    private boolean isAccountNameTaken(Long userId, String productName, Set<String> usedNames) {
        if (usedNames.contains(productName)) return true; // 이번 실행 중 중복
        // DB 중복 체크(타입 무관 전체 계좌에서 이름 충돌 막음)
        return accountMapper.countByUserAndProductName(userId, productName) > 0;
    }

    private boolean isCardNameTaken(Long userId, String cardName, Set<String> usedNames) {
        if (usedNames.contains(cardName)) return true; // 이번 실행 중 중복
        return cardMapper.countByUserAndCardName(userId, cardName) > 0;
    }

    private static record AccountBrand(String bankCode, String productName) {}
    private static record CardBrand(String bankName, String cardName, String cardType) {
        public String cardType(){ return cardType; }
    }

    // 3-2-6 형식 예: 123-45-678901 (실서비스 규격 맞추면 변경)
    private String generateAccountNumber(String bankCode, Long userId, int seq) {
        SplittableRandom r = new SplittableRandom(Objects.hash(bankCode, userId, seq, System.nanoTime()));
        int p1 = 100 + r.nextInt(900);
        int p2 = 10 + r.nextInt(90);
        int p3 = 100000 + r.nextInt(900000);
        return String.format("%03d-%02d-%06d", p1, p2, p3);
    }

    /** 1234-****-****-5678 형식 */
    private String generateMaskedCardNumber(Long userId, int seq) {
        SplittableRandom r = new SplittableRandom(Objects.hash("CARD", userId, seq, System.nanoTime()));
        int first = 1000 + r.nextInt(9000);
        int last  = 1000 + r.nextInt(9000);
        return String.format("%04d-****-****-%04d", first, last);
    }

    private BigDecimal readBalanceSafe(String finAcno) {
        try {
            return new BigDecimal(nhApiClient.callInquireBalance(finAcno).optString("Ldbl", "900000"));
        } catch (Exception e) {
            log.warn("잔액 조회 실패(finAcno={}), 기본값 사용", finAcno, e);
            return new BigDecimal("900000");
        }
    }
}
