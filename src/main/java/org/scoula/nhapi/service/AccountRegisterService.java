package org.scoula.nhapi.service;

import org.scoula.nhapi.dto.FinAccountRequestDto;

import java.util.Map;

public interface AccountRegisterService {
    Map<String, Object> registerAccount(FinAccountRequestDto requestDto);
}
