package org.scoula.nhapi.service;

import org.scoula.nhapi.dto.FinAccountRequestDto;

import java.math.BigDecimal;
import java.util.List;
import org.scoula.nhapi.dto.TransactionDto;


public interface NhAccountService {

    String callOpenFinAccount(FinAccountRequestDto dto);
    BigDecimal callInquireBalance(String finAcno);
    List<TransactionDto> callTransactionList(String finAcno, String fromDate, String toDate);
}
