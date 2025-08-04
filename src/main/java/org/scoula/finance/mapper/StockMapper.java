package org.scoula.finance.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.finance.dto.stock.StockAccessTokenDto;
import org.scoula.finance.dto.stock.StockAccountDto;
import org.scoula.finance.dto.stock.StockChartDataDto;
import org.scoula.finance.dto.stock.StockListDataDto;

import java.util.List;

@Mapper
public interface StockMapper {

    void saveOrUpdateToken(StockAccessTokenDto dto);

    String getUserAccount(Long userId);

    String getUserToken(Long userId);

    List<StockListDataDto> getStockList();


    int updateStockReturnsData(@Param("stockCode") String stockCode,
                               @Param("stockReturnsData") String stockReturnsData);

    List<String> getStockCodeList();

    String getChartCache(String stockCode);
}
