package org.scoula.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.account.domain.Account;
import org.scoula.account.dto.AccountRegisterResponseDto;
import org.scoula.account.mapper.AccountMapper;
import org.scoula.common.exception.BaseException;
import org.scoula.nhapi.dto.FinAccountRequestDto;
import org.scoula.nhapi.service.NhAccountService;
import org.scoula.transactions.service.TransactionSyncProcessor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final NhAccountService nhAccountService;
    private final AccountMapper accountMapper;
    private final TransactionSyncProcessor transactionSyncProcessor;

    @Override
    public AccountRegisterResponseDto registerFinAccount(FinAccountRequestDto dto) {
        // NH API를 통해 핀어카운트 발급
        String finAcno = nhAccountService.callOpenFinAccount(dto);

        // NH API로 초기 잔액 조회
        BigDecimal balance = nhAccountService.callInquireBalance(finAcno);

        // 계좌 저장
        Account account = Account.builder()
                .userId(1L) // 하드코딩된 userId (실제 로직에서는 로그인된 사용자로 대체)
                .pinAccountNumber(finAcno)
                .bankCode("011")                   // 하드코딩된 은행코드
                .accountNumber(dto.getAccountNumber())
                .productName("KB IT's Your Life 6기 통장") // 하드코딩된 상품명
                .accountType("DEPOSIT")           // 기본값
                .balance(balance)                 // 초기 잔액
                .createdAt(LocalDateTime.now())
                .build();

        accountMapper.insert(account);

        transactionSyncProcessor.syncAccountTransactions(account, true);

        log.info("✅ 계좌 등록 및 초기화 성공: {}", account);
        return AccountRegisterResponseDto.builder()
                .accountId(account.getId())
                .finAccount(finAcno)
                .balance(balance)
                .build();
    }

    @Override
    public void syncAccountById(Long accountId) {
        Account account = accountMapper.findById(accountId);
        if (account == null) throw new BaseException("해당 계좌가 존재하지 않습니다.", 404);

        // 거래내역 동기화 (isInitial = false)
        transactionSyncProcessor.syncAccountTransactions(account, false);

        // 잔액 최신화
        BigDecimal newBalance = nhAccountService.callInquireBalance(account.getPinAccountNumber());
        accountMapper.updateBalanceByUser(account.getUserId(), account.getPinAccountNumber(), newBalance);

        log.info("✅ 계좌 {} 동기화 완료 (최신 잔액: {})", account.getId(), newBalance);
    }

    @Override
    public void syncAllAccountsByUserId(Long userId) {
        List<Account> accounts = accountMapper.findByUserId(userId);
        for (Account acc : accounts) {
            transactionSyncProcessor.syncAccountTransactions(acc, false);
            BigDecimal newBalance = nhAccountService.callInquireBalance(acc.getPinAccountNumber());
            accountMapper.updateBalanceByUser(userId, acc.getPinAccountNumber(), newBalance);
        }
    }


}