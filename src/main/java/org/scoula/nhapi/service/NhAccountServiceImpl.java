package org.scoula.nhapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.scoula.nhapi.client.NHApiClient;
import org.scoula.nhapi.dto.FinAccountRequestDto;
import org.scoula.nhapi.dto.NhAccountTransactionResponseDto;
import org.scoula.nhapi.exception.NHApiException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NhAccountServiceImpl implements NhAccountService {

    private final NHApiClient nhApiClient;

    @Override
    public String callOpenFinAccount(FinAccountRequestDto dto) {
        dto.validate();

        JSONObject res = nhApiClient.callOpenFinAccount(dto.getAccountNumber(), dto.getBirthday());
        log.info("🔍 핀어카운트 발급 응답: {}", res.toString());

        String rpcd = res.getJSONObject("Header").getString("Rpcd");
        if ("A0013".equals(rpcd)) throw new NHApiException("이미 등록된 핀어카운트입니다.");
        if (!"00000".equals(rpcd)) throw new NHApiException("핀어카운트 발급 실패: " + rpcd);

        String rgno = res.optString("Rgno");
        if (rgno == null) throw new NHApiException("Rgno가 응답에 없습니다.");

        for (int attempt = 1; attempt <= 3; attempt++) {
            JSONObject checkRes = nhApiClient.callCheckFinAccount(rgno, dto.getBirthday());
            log.info("🔁 [{}] 핀어카운트 확인 응답: {}", attempt, checkRes.toString());

            if ("00000".equals(checkRes.getJSONObject("Header").getString("Rpcd"))) {
                String finAcno = checkRes.optString("FinAcno");
                if (finAcno != null) return finAcno;
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        throw new NHApiException("핀어카운트 확인 실패 (FinAcno 누락)");
    }

    @Override
    public BigDecimal callInquireBalance(String finAcno) {
        JSONObject res = nhApiClient.callInquireBalance(finAcno);
        String rpcd = res.getJSONObject("Header").getString("Rpcd");

        if (!"00000".equals(rpcd)) throw new NHApiException("잔액 조회 실패: " + rpcd);
        return new BigDecimal(res.getString("Ldbl"));
    }

    @Override
    public List<NhAccountTransactionResponseDto> callTransactionList(Long userId, Long accountId, String finAcno, String from, String to) {
        JSONObject res = nhApiClient.callTransactionList(finAcno, from, to);
        String rpcd = res.getJSONObject("Header").getString("Rpcd");

        if ("A0090".equals(rpcd)) {
            log.info("✅ 거래내역 없음, 더미 데이터 반환 (핀어카운트: {})", finAcno);
            return createDummyTransactions(userId, accountId);
        }
        if (!"00000".equals(rpcd)) throw new NHApiException("거래내역 조회 실패");

        JSONArray arr = res.getJSONArray("Rec");
        List<NhAccountTransactionResponseDto> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            list.add(NhAccountTransactionResponseDto.from(arr.getJSONObject(i)));
        }

        return list.isEmpty() ? createDummyTransactions(userId, accountId) : list;
    }

    private List<NhAccountTransactionResponseDto> createDummyTransactions(Long userId, Long accountId) {
        List<NhAccountTransactionResponseDto> dummyList = new ArrayList<>();
        LocalDateTime base = LocalDateTime.of(2025, 4, 1, 9, 0);
        BigDecimal balance = BigDecimal.valueOf(1000000);

        for (int i = 0; i < 50; i++) {
            boolean isIncome = i % 5 == 0;
            BigDecimal amount = isIncome ? BigDecimal.valueOf(200000 + i * 1000) : BigDecimal.valueOf(10000 + i * 300);
            balance = isIncome ? balance.add(amount) : balance.subtract(amount);

            dummyList.add(NhAccountTransactionResponseDto.builder()
                    .userId(userId)
                    .accountId(accountId)
                    .date(base.plusDays(i))
                    .type(isIncome ? "INCOME" : "EXPENSE")
                    .amount(amount)
                    .balance(balance)
                    .place(isIncome ? "입금처" : "지출처")
                    .tuNo(100000L + i)
                    .isCancelled(false)
                    .memo(isIncome ? "월급" : null)
                    .category(null)
                    .analysis(null)
                    .build());
        }

        return dummyList;
    }
}
