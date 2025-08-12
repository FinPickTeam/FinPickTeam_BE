package org.scoula.coin.service;

import org.scoula.coin.dto.CoinMonthlyResponseDTO;
import org.scoula.coin.dto.CoinStatusResponseDTO;

public interface CoinService {
    CoinMonthlyResponseDTO getMyMonthlyCoin(Long userId);
    CoinStatusResponseDTO getMyCoinStatus(Long userId);
}
