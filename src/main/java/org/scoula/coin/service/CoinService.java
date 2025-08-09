package org.scoula.coin.service;

import org.scoula.coin.dto.CoinMonthlyResponseDTO;

public interface CoinService {
    CoinMonthlyResponseDTO getMyMonthlyCoin(Long userId);
}
