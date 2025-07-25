package org.scoula.finance.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.scoula.finance.dto.stock.StockAccessTokenDto;
import org.scoula.finance.dto.stock.StockAccountDto;
import org.scoula.finance.dto.stock.StockChartDataDto;
import org.scoula.finance.dto.stock.StockListDataDto;

import java.util.List;

@Mapper
public interface StockMapper {

    /**
     * 사용자 토큰 저장 또는 갱신
     * @param dto 사용자 토큰 정보 DTO
     */
    void saveOrUpdateToken(StockAccessTokenDto dto);

    /**
     * 사용자 계좌 정보 조회
     * @param userId 사용자 ID
     * @return 계좌 번호 (문자열)
     */
    String getUserAccount(Long userId);

    /**
     * 사용자 토큰 조회
     * @param userId 사용자 ID
     * @return 사용자 인증 토큰
     */
    String getUserToken(Long userId);

    /**
     * 사용자 보유 주식 리스트 조회
     * @return 주식 코드, 이름, 요약 등의 정보 리스트
     */
    List<StockListDataDto> getStockList();

    /**
     * 차트 데이터 캐시 저장 (기존 데이터가 있으면 덮어쓰기)
     * @param dto 차트 데이터 DTO (stock_code, jsonData, baseDate 등 포함)
     */
    void saveChartCache(StockChartDataDto dto);

    /**
     * 사용자 주식 코드 목록 조회 (차트 조회용 등)
     * @return 주식 코드 리스트
     */
    List<String> getStockCodeList();

    /**
     * 주식 차트에 쓰일 5일치 데이터 반환
     * @param stockCode 주식 종목 코드
     * @return 5일치 차트 데이터
     */
    String getChartCache(String stockCode);
}
