package org.scoula.nhapi.service;

import org.scoula.nhapi.dto.FinAccountRequestDto;

import java.math.BigDecimal;
import java.util.List;
import org.scoula.nhapi.dto.NhAccountTransactionResponseDto;


public interface NhAccountService {

    String callOpenFinAccount(FinAccountRequestDto dto);
    BigDecimal callInquireBalance(String finAcno);
    List<NhAccountTransactionResponseDto> callTransactionList(Long userId, Long accountId, String finAcno, String from, String to);
}
