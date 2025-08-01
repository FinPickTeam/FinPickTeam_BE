// ✅ 1. 패키지 선언
package org.scoula.transactions.service;

// ✅ 2. import 문
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.account.domain.Account;
import org.scoula.nhapi.dto.TransactionDto;
import org.scoula.nhapi.service.NhAccountService;
import org.scoula.transactions.domain.AccountTransaction;
import org.scoula.transactions.domain.Ledger;
import org.scoula.transactions.mapper.AccountTransactionMapper;
import org.scoula.transactions.mapper.LedgerMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 계좌 거래내역 동기화를 처리하는 프로세서
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionSyncProcessor {

    private final NhAccountService nhAccountService;
    private final AccountTransactionMapper accountTransactionMapper;
    private final LedgerMapper ledgerMapper;

    /**
     * 계좌 기반으로 NH 거래내역 동기화 처리
     */
    public void syncAccountTransactions(Account account) {
        String finAcno = account.getPinAccountNumber();
        Long userId = account.getUserId();
        Long accountId = account.getId();

        // 1. 거래 기간: 최근 3개월
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusMonths(3);

        List<TransactionDto> nhTransactions = nhAccountService.callTransactionList(
                finAcno,
                from.format(DateTimeFormatter.BASIC_ISO_DATE),
                to.format(DateTimeFormatter.BASIC_ISO_DATE)
        );

        for (TransactionDto dto : nhTransactions) {
            // 2. 거래내역 저장
            AccountTransaction tx = AccountTransaction.builder()
                    .userId(userId)
                    .accountId(accountId)
                    .date(dto.getDate())
                    .type(dto.getType())
                    .amount(dto.getAmount())
                    .balance(account.getBalance()) // API 응답에는 잔액 정보 없음 → 계좌 기준
                    .place(dto.getPlace())
                    .isCancelled(false)
                    .tuNo(System.nanoTime()) // 임시 고유값
                    .build();

            accountTransactionMapper.insert(tx);

            // 3. INCOME만 ledger에 저장
            if ("INCOME".equals(dto.getType())) {
                Ledger ledger = Ledger.builder()
                        .userId(userId)
                        .sourceId(tx.getId()) // account_transaction의 ID
                        .accountId(accountId)
                        .sourceType("ACCOUNT")
                        .sourceName(account.getProductName())
                        .type("INCOME")
                        .amount(dto.getAmount())
                        .category(null)
                        .memo(dto.getMemo())
                        .analysis(null)
                        .date(dto.getDate())
                        .merchantName(null)
                        .place(dto.getPlace())
                        .build();

                ledgerMapper.insert(ledger);
            }
        }

        log.info("✅ 거래내역 동기화 완료: 총 {}건", nhTransactions.size());
    }
}
