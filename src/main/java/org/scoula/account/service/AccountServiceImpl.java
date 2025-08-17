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
import org.scoula.nhapi.client.NHApiClient;
import org.scoula.nhapi.dto.FinAccountRequestDto;
import org.scoula.nhapi.service.NhAccountService;
import org.scoula.nhapi.util.FirstLinkOnboardingService;
import org.scoula.nhapi.util.MaskingUtil;
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
    private final NHApiClient nhApiClient; // MOCK 직행용
    private final AccountMapper accountMapper;
    private final AccountTransactionService accountTransactionService;
    private final FirstLinkOnboardingService firstLinkOnboardingService;

    @Override
    public AccountRegisterResponseDto registerFinAccount(Long userId, FinAccountRequestDto dto) {

        // ① DTO 비거나 placeholder면 → MOCK 경로(검증 미탑)
        if (isEmpty(dto)) {
            var brand = org.scoula.nhapi.util.AccountBrandingUtil.pickDeposit(userId);

            String finAcno = nhApiClient
                    .callCheckFinAccount("RG" + System.nanoTime(), "19900101")
                    .optString("FinAcno");

            BigDecimal balance = nhAccountService.callInquireBalance(finAcno);

            // 브랜드 제공 계좌번호도 가운데 마스킹해서 저장
            String displayAccNo = MaskingUtil.maskAccount(brand.accountNumber());

            Account account = Account.builder()
                    .userId(userId)
                    .pinAccountNumber(finAcno)
                    .bankCode(brand.bankCode())
                    .accountNumber(displayAccNo)     // 마스킹 저장
                    .productName(brand.productName())
                    .accountType("DEPOSIT")
                    .balance(balance)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            accountMapper.insert(account);

            // 첫 연동 패키지(부족분 채우기)
            firstLinkOnboardingService.runOnceOnFirstLink(userId);

            log.info("✅ [MOCK] 계좌 등록 완료: {}", account);
            return AccountRegisterResponseDto.builder()
                    .accountId(account.getId())
                    .finAccount(finAcno)
                    .balance(balance)
                    .build();
        }

        // ② 정상 경로
        String finAcno = nhAccountService.callOpenFinAccount(dto);
        BigDecimal balance = nhAccountService.callInquireBalance(finAcno);

        Account account = Account.builder()
                .userId(userId)
                .pinAccountNumber(finAcno)
                .bankCode("011")
                .accountNumber(MaskingUtil.maskAccount(dto.getAccountNumber())) // 입력값도 마스킹 저장
                .productName("KB IT's Your Life 6기 통장")
                .accountType("DEPOSIT")
                .balance(balance)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        accountMapper.insert(account);
        accountTransactionService.syncAccountTransactions(account, true);
        firstLinkOnboardingService.runOnceOnFirstLink(userId);

        log.info("✅ 계좌 등록 및 초기화 성공: {}", account);
        return AccountRegisterResponseDto.builder()
                .accountId(account.getId())
                .finAccount(finAcno)
                .balance(balance)
                .build();
    }

    @Override
    public void syncAccountById(Long userId, Long accountId) {
        Account account = accountMapper.findById(accountId);
        if (account == null) throw new BaseException("해당 계좌가 존재하지 않습니다.", 404);
        if (!account.getUserId().equals(userId)) throw new ForbiddenException("본인 계좌만 동기화할 수 있습니다");
        if (!Boolean.TRUE.equals(account.getIsActive())) throw new BaseException("비활성화된 계좌입니다.", 400);

        accountTransactionService.syncAccountTransactions(account, false);

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
        if (account == null || !account.getUserId().equals(userId)) throw new ForbiddenException("본인 계좌만 삭제할 수 있습니다");
        if (!Boolean.TRUE.equals(account.getIsActive())) return;
        accountMapper.updateIsActive(accountId, false);
    }

    @Override
    public AccountListWithTotalDto getAccountsWithTotal(Long userId) {
        List<Account> accounts = accountMapper.findActiveByUserId(userId);
        List<AccountDto> dtoList = accounts.stream().map(AccountDto::from).collect(Collectors.toList());
        BigDecimal total = accountMapper.sumBalanceByUserId(userId);
        AccountListWithTotalDto dto = new AccountListWithTotalDto();
        dto.setAccountTotal(total);
        dto.setAccounts(dtoList);
        return dto;
    }

    /* ===== 헬퍼 ===== */
    private static boolean isEmpty(FinAccountRequestDto dto) {
        return dto == null
                || blankLike(dto.getAccountNumber())
                || blankLike(dto.getBirthday());
    }

    private static boolean blankLike(String s) {
        if (s == null) return true;
        String v = s.trim();
        return v.isEmpty()
                || v.equalsIgnoreCase("string")
                || v.equalsIgnoreCase("null")
                || v.equalsIgnoreCase("undefined");
    }
}
