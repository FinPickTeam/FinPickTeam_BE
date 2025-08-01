package org.scoula.account.service;

import lombok.RequiredArgsConstructor;
import org.scoula.account.domain.Account;
import org.scoula.account.dto.AccountRegisterResponseDto;
import org.scoula.account.mapper.AccountMapper;
import org.scoula.nhapi.dto.FinAccountRequestDto;
import org.scoula.nhapi.service.NhAccountService;
import org.scoula.transactions.service.TransactionSyncProcessor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final NhAccountService nhAccountService;
    private final AccountMapper accountMapper;
    private final TransactionSyncProcessor transactionSyncProcessor;

    @Override
    public AccountRegisterResponseDto registerFinAccount(FinAccountRequestDto dto) {
        // 1. NH API를 통해 핀어카운트 발급 및 잔액 조회
        String finAcno = nhAccountService.callOpenFinAccount(dto);
        BigDecimal balance = nhAccountService.callInquireBalance(finAcno);

        // 2. 계좌 정보 저장
        Account account = Account.builder()
                .userId(1L)  // TODO: 실제 로그인한 사용자 ID로 교체
                .pinAccountNumber(finAcno)
                .bankCode("011")
                .accountNumber(dto.getAccountNumber())
                .productName("NH입출금통장")  // TODO: 향후 사용자 입력 or API 연동
                .accountType("DEPOSIT")
                .balance(balance)
                .build();

        accountMapper.insert(account);                          // 계좌 저장
        accountMapper.updateBalance(finAcno, balance);         // 잔액 갱신

        // 3. 거래내역 동기화 실행
        transactionSyncProcessor.syncAccountTransactions(account);

        // 4. 등록 결과 응답
        return AccountRegisterResponseDto.builder()
                .accountId(account.getId())
                .finAccount(finAcno)
                .balance(balance)
                .build();
    }
}
