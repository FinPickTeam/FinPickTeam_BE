package org.scoula.account.service;

import org.scoula.account.dto.AccountDto;
import org.scoula.account.dto.AccountListWithTotalDto;
import org.scoula.nhapi.dto.FinAccountRequestDto;
import org.scoula.account.dto.AccountRegisterResponseDto;

import java.util.List;

public interface AccountService {
    AccountRegisterResponseDto registerFinAccount(FinAccountRequestDto dto);
    void syncAccountById(Long accountId);
    void syncAllAccountsByUserId(Long userId);
    void deactivateAccount(Long accountId, Long userId);;
    AccountListWithTotalDto getAccountsWithTotal(Long userId);

}
