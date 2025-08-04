package org.scoula.transactions.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.account.domain.Account;
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

    @Override
    public List<AccountTransactionDto> getTransactions(Long userId, Long accountId, String from, String to) {
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
                : getLastSyncDate(account.getId(), to);

        List<NhAccountTransactionResponseDto> dtoList = nhAccountService.callTransactionList(
                userId, account.getId(), finAcno,
                from.format(DateTimeFormatter.BASIC_ISO_DATE),
                to.format(DateTimeFormatter.BASIC_ISO_DATE)
        );

        for (NhAccountTransactionResponseDto dto : dtoList) {
            if (accountTransactionMapper.existsByUserIdAndTuNo(userId, dto.getTuNo())) continue;

            AccountTransaction tx = new AccountTransaction(dto, userId, account.getId());
            accountTransactionMapper.insert(tx);

            if (dto.isIncome()) {
                int categoryId = 10; // "이체" 고정 ID
                Ledger ledger = new Ledger(tx, account.getProductName(), categoryId);
                ledgerMapper.accountInsert(ledger);
            }

        }

        log.info("✅ 계좌 {} 거래내역 동기화 완료 ({}건)", account.getId(), dtoList.size());
    }

    private LocalDate getLastSyncDate(Long accountId, LocalDate fallback) {
        LocalDateTime last = accountTransactionMapper.findLastTransactionDate(accountId);
        return last != null ? last.toLocalDate().plusDays(1) : fallback.minusMonths(3);
    }

    @Override
    public void syncAccountTransactions(Account account) {
        syncAccountTransactions(account, true);
    }
}
