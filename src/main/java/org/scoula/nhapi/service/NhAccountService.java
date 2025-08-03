package org.scoula.nhapi.service;

import org.scoula.nhapi.dto.FinAccountRequestDto;

import java.math.BigDecimal;
import java.util.List;
import org.scoula.nhapi.dto.NhTransactionResponseDto;


public interface NhAccountService {

    String callOpenFinAccount(FinAccountRequestDto dto);
    BigDecimal callInquireBalance(String finAcno);
    List<NhTransactionResponseDto> callTransactionList(Long userId, Long accountId, String finAcno, String from, String to);
}
