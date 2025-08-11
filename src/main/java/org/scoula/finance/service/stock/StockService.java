package org.scoula.finance.service.stock;

import org.scoula.finance.dto.stock.*;

import java.util.List;

public interface StockService {
    // 키움증권 API에서 토큰 발급 → DB 저장 → 저장된 DTO 반환
    StockAccessTokenDto issueAndSaveToken(Long userId);

    // 사용자 계좌 수익률 조회(상세 정보 X)
    StockAccountDto getAccountReturnRate(Long userId);

    //주식 리스트 조회
    List<StockListDto> getStockList(Long userId, StockFilterDto filterDto);

    // 차트 데이터 DB에 저장
    void updateChartData();

    // 팩터 계산 및 DB에 저장
    void updateFactor(String analyzeDate, String resultDate, String startDate);

    // 주식 상세 정보 조회
    StockDetailDto getStockDetail(Long userId, String stockCode);

    // 사용자 맞춤 추식 추천
    List<StockListDto> getStockRecommendationList(Long userId, int limit, Integer amount);

    String getStockReturn(String stockCode, String startDate, String endDate);
}