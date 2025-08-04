package org.scoula.nhapi.service;

import org.scoula.nhapi.dto.NhCardTransactionResponseDto;

import java.util.List;

public interface NhCardService {
    List<NhCardTransactionResponseDto> callCardTransactionList(Long userId, String finCard, String from, String to);
}

