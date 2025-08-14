package org.scoula.finance.mapper;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.finance.dto.stock.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface StockMapper {

    void saveOrUpdateToken(StockAccessTokenDto dto);

    String getUserAccount(Long userId);

    String getUserToken(Long userId);

    List<StockListDataDto> getStockList(StockFilterDto filter);


    void updateStockReturnsData(@Param("stockCode") String stockCode,
                                @Param("stockReturnsData") String stockReturnsData);

    @MapKey("stock_code")
    List<Map<String,Object>> getStockCodeList();

    StockListDataDto getStockListDataByStockCode(String stockCode);

    void insertStockFactorData(StockFactorDto stockFactorDto);

    List<StockFactorDto> getStockFactorData();

    String getStockDivByStockCode(String stockCode);
}
