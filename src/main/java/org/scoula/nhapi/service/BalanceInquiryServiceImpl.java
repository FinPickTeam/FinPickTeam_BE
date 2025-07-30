package org.scoula.nhapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.nhapi.dto.BalanceResponseDto;
import org.scoula.nhapi.mapper.NhAccountMapper;
import org.scoula.nhapi.util.NHApiClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceInquiryServiceImpl implements BalanceInquiryService {

    private final NHApiClient nhApiClient;
    private final NhAccountMapper nhAccountMapper;

    @Override
    public BalanceResponseDto getBalance(String finAccount) {
        BigDecimal balance = nhApiClient.callInquireBalance(finAccount);
        nhAccountMapper.updateBalance(finAccount, balance);
        return BalanceResponseDto.builder()
                .finAccount(finAccount)
                .balance(balance)
                .build();
    }
}
