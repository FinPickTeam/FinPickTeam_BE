package org.scoula.account.service;

import org.scoula.nhapi.dto.FinAccountRequestDto;
import org.scoula.account.dto.AccountRegisterResponseDto;

public interface AccountService {

    AccountRegisterResponseDto registerFinAccount(FinAccountRequestDto dto);
}
