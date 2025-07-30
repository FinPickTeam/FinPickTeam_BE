package org.scoula.nhapi.service;

import org.scoula.nhapi.dto.BalanceResponseDto;

public interface BalanceInquiryService {
    BalanceResponseDto getBalance(String finAccount);
}
