package org.scoula.nhapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.nhapi.domain.Account;
import org.scoula.nhapi.dto.FinAccountRequestDto;
import org.scoula.nhapi.mapper.NhAccountMapper;
import org.scoula.nhapi.util.NHApiClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountRegisterServiceImpl implements AccountRegisterService {

    private final NHApiClient nhApiClient;
    private final NhAccountMapper nhAccountMapper;

    @Override
    public Map<String, Object> registerAccount(FinAccountRequestDto requestDto) {
        // 1. 핀어카운트 발급 + 발급확인
        String finAcno = nhApiClient.callOpenFinAccount(requestDto);

        // 2. 계좌 생성
        Account account = Account.builder()
                .userId(1L) // ✅ 나중에 로그인된 사용자로 교체
                .bankName("NH농협")
                .accountNumber(requestDto.getAccountNumber())
                .productName("입출금통장")
                .interestRate(1.2f)
                .pinAccountNumber(finAcno)
                .accountType("입출금")
                .balance(BigDecimal.ZERO)
                .connectedAt(LocalDateTime.now())
                .build();

        nhAccountMapper.insertAccount(account);

        // 3. 잔액 조회 → balance 업데이트
        BigDecimal balance = nhApiClient.callInquireBalance(finAcno);
        nhAccountMapper.updateBalance(finAcno, balance);

        log.info("✅ 계좌 등록 및 잔액 업데이트 완료. FinAcno: {}, Balance: {}", finAcno, balance);

        // 4. accountId 조회 (추후 기능 연결에 유용)
        Long accountId = nhAccountMapper.findIdByFinAccount(finAcno);

        return Map.of(
                "accountId", accountId,
                "finAccount", finAcno,
                "balance", balance,
                "message", "계좌 등록 및 잔액 반영 완료"
        );
    }
}
