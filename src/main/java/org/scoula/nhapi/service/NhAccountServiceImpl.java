package org.scoula.nhapi.service;

import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.scoula.account.domain.Account;
import org.scoula.account.mapper.AccountMapper;
import org.scoula.account.dto.AccountRegisterResponseDto;
import org.scoula.nhapi.client.NHApiClient;
import org.scoula.nhapi.dto.FinAccountRequestDto;
import org.scoula.nhapi.dto.TransactionDto;
import org.scoula.nhapi.exception.NHApiException;
import org.scoula.transactions.service.TransactionSyncProcessor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class NhAccountServiceImpl implements NhAccountService {

    private final NHApiClient nhApiClient;

    @Override
    public String callOpenFinAccount(FinAccountRequestDto dto) {
        JSONObject res = nhApiClient.callOpenFinAccount(dto);
        String rpcd = res.getJSONObject("Header").getString("Rpcd");

        if ("A0013".equals(rpcd)) {
            throw new NHApiException("이미 등록된 핀어카운트입니다. DB 확인 필요");
        }
        if (!"00000".equals(rpcd)) {
            throw new NHApiException("핀어카운트 발급 실패: " + rpcd);
        }

        String rgno = res.getString("Rgno");
        JSONObject checkRes = nhApiClient.callCheckFinAccount(rgno, dto.getBirthday());

        String checkRpcd = checkRes.getJSONObject("Header").getString("Rpcd");
        if (!"00000".equals(checkRpcd)) {
            throw new NHApiException("핀어카운트 확인 실패: " + checkRpcd);
        }

        return checkRes.getString("FinAcno");
    }

    @Override
    public BigDecimal callInquireBalance(String finAcno) {
        JSONObject res = nhApiClient.callInquireBalance(finAcno);
        String rpcd = res.getJSONObject("Header").getString("Rpcd");

        if (!"00000".equals(rpcd)) {
            throw new NHApiException("잔액 조회 실패: " + rpcd);
        }

        return new BigDecimal(res.getString("Ldbl"));
    }

    @Override
    public List<TransactionDto> callTransactionList(String finAcno, String from, String to) {
        JSONObject res = nhApiClient.callTransactionList(finAcno, from, to);
        String rpcd = res.getJSONObject("Header").getString("Rpcd");

        if ("A0090".equals(rpcd)) return List.of();
        if (!"00000".equals(rpcd)) throw new NHApiException("거래내역 조회 실패");

        JSONArray arr = res.getJSONArray("Rec");
        List<TransactionDto> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            list.add(TransactionDto.from(obj));
        }
        return list;
    }
}
