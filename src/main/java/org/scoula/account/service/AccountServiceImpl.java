package org.scoula.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.account.domain.Account;
import org.scoula.account.dto.AccountDto;
import org.scoula.account.dto.AccountListWithTotalDto;
import org.scoula.account.dto.AccountRegisterResponseDto;
import org.scoula.account.mapper.AccountMapper;
import org.scoula.common.exception.BaseException;
import org.scoula.common.exception.ForbiddenException;
import org.scoula.nhapi.dto.FinAccountRequestDto;
import org.scoula.nhapi.service.NhAccountService;
import org.scoula.transactions.service.AccountTransactionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final NhAccountService nhAccountService;
    private final AccountMapper accountMapper;
    private final AccountTransactionService accountTransactionService;

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

        accountTransactionService.syncAccountTransactions(account, true);

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
        if (!Boolean.TRUE.equals(account.getIsActive())) {
            throw new BaseException("비활성화된 계좌입니다.", 400);
        }
        // 거래내역 동기화 (isInitial = false)
        accountTransactionService.syncAccountTransactions(account, false);

        // 잔액 최신화
        BigDecimal newBalance = nhAccountService.callInquireBalance(account.getPinAccountNumber());
        accountMapper.updateBalanceByUser(account.getUserId(), account.getPinAccountNumber(), newBalance);

        log.info("✅ 계좌 {} 동기화 완료 (최신 잔액: {})", account.getId(), newBalance);
    }

    @Override
    public void syncAllAccountsByUserId(Long userId) {
        List<Account> accounts = accountMapper.findActiveByUserId(userId);
        for (Account acc : accounts) {
            accountTransactionService.syncAccountTransactions(acc, false);
            BigDecimal newBalance = nhAccountService.callInquireBalance(acc.getPinAccountNumber());
            accountMapper.updateBalanceByUser(userId, acc.getPinAccountNumber(), newBalance);
        }
    }

    @Override
    public void deactivateAccount(Long accountId, Long userId) {
        Account account = accountMapper.findById(accountId);

        if (account == null || !account.getUserId().equals(userId)) {
            throw new ForbiddenException("본인 계좌만 삭제할 수 있습니다");
        }

        if (!Boolean.TRUE.equals(account.getIsActive())) {
            return; // 이미 비활성화면 그냥 무시
        }

        accountMapper.updateIsActive(accountId, false);
    }

    @Override
    public AccountListWithTotalDto getAccountsWithTotal(Long userId) {
        List<Account> accounts = accountMapper.findActiveByUserId(userId);
        List<AccountDto> dtoList = accounts.stream()
                .map(AccountDto::from) // AccountDto에 static from(Account account) 만들어서 변환
                .collect(Collectors.toList());
        BigDecimal total = accountMapper.sumBalanceByUserId(userId);
        AccountListWithTotalDto dto = new AccountListWithTotalDto();
        dto.setAccountTotal(total);
        dto.setAccounts(dtoList);
        return dto;
    }

}