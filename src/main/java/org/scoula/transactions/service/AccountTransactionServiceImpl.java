package org.scoula.transactions.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.account.domain.Account;
import org.scoula.account.mapper.AccountMapper;
import org.scoula.common.exception.BaseException;
import org.scoula.nhapi.dto.NhAccountTransactionResponseDto;
import org.scoula.nhapi.service.NhAccountService;
import org.scoula.transactions.domain.AccountTransaction;
import org.scoula.transactions.domain.Ledger;
import org.scoula.transactions.dto.AccountTransactionDto;
import org.scoula.transactions.exception.AccountTransactionNotFoundException;
import org.scoula.transactions.mapper.AccountTransactionMapper;
import org.scoula.transactions.mapper.LedgerMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountTransactionServiceImpl implements AccountTransactionService {

    private final NhAccountService nhAccountService;
    private final AccountTransactionMapper accountTransactionMapper;
    private final LedgerMapper ledgerMapper;
    private final AccountMapper accountMapper;

    @Override
    public List<AccountTransactionDto> getTransactions(Long userId, Long accountId, String from, String to) {
        Account acc = accountMapper.findById(accountId);
        if (acc == null) throw new BaseException("해당 계좌가 존재하지 않습니다.", 404);
        if (!Boolean.TRUE.equals(acc.getIsActive())) {
            throw new BaseException("비활성화된 계좌입니다.", 400);
        }

        List<AccountTransaction> txList = accountTransactionMapper.findAccountTransactions(userId, accountId, from, to);
        if (txList == null || txList.isEmpty()) {
            throw new AccountTransactionNotFoundException();
        }

        return txList.stream().map(this::toDto).toList();
    }

    private AccountTransactionDto toDto(AccountTransaction tx) {
        AccountTransactionDto dto = new AccountTransactionDto();
        dto.setId(tx.getId());
        dto.setUserId(tx.getUserId());
        dto.setAccountId(tx.getAccountId());
        dto.setDate(tx.getDate());
        dto.setType(tx.getType());
        dto.setAmount(tx.getAmount());
        dto.setBalance(tx.getBalance());
        dto.setPlace(tx.getPlace());
        return dto;
    }

    @Override
    public void syncAccountTransactions(Account account, boolean isInitial) {
        Long userId = account.getUserId();
        String finAcno = account.getPinAccountNumber();

        LocalDate to = LocalDate.now();
        LocalDate from = isInitial ? to.minusMonths(3)
                : getNextStartDate(account.getId(), to); // 마지막+1일

        List<NhAccountTransactionResponseDto> dtoList = nhAccountService.callTransactionList(
                userId, account.getId(), finAcno,
                from.format(DateTimeFormatter.BASIC_ISO_DATE),
                to.format(DateTimeFormatter.BASIC_ISO_DATE)
        );

        for (NhAccountTransactionResponseDto dto : dtoList) {
            if (accountTransactionMapper.existsByUserIdAndAccountIdAndTuNo(userId, account.getId(), dto.getTuNo())) continue;

            AccountTransaction tx = new AccountTransaction(dto, userId, account.getId());
            accountTransactionMapper.insert(tx);

            if (dto.isIncome()) {
                int categoryId = 10; // "이체"
                Ledger ledger = new Ledger(tx, account.getProductName(), categoryId);
                ledgerMapper.accountInsert(ledger);
            }
        }

        log.info("✅ 계좌 {} 거래내역 동기화 완료 ({}건, {} ~ {})", account.getId(), dtoList.size(), from, to);
    }

    // 기본은 증분 동기화로
    @Override
    public void syncAccountTransactions(Account account) {
        syncAccountTransactions(account, false);
    }

    // 마지막 거래일의 다음날을 시작점으로(미존재 시 3개월 전)
    private LocalDate getNextStartDate(Long accountId, LocalDate today) {
        LocalDateTime last = accountTransactionMapper.findLastTransactionDate(accountId);
        return last != null ? last.toLocalDate().plusDays(1) : today.minusMonths(3);
    }
}
