package org.scoula.finance.service.stock;

import org.scoula.finance.dto.stock.StockAccessTokenDto;
import org.scoula.finance.dto.stock.StockAccountDto;

public interface StockService {
    // 키움 API에서 토큰 발급 → DB 저장 → 저장된 DTO 반환
    StockAccessTokenDto issueAndSaveToken(Long userId);

    //사용자 계좌 수익률 조회(상세 정보 X)
    StockAccountDto getAccountReturnRate(Long userId);
}