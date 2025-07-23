package org.scoula.finance.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.scoula.finance.dto.stock.StockAccessTokenDto;
import org.scoula.finance.dto.stock.StockAccountDto;

@Mapper
public interface StockMapper {
    void saveOrUpdateToken(StockAccessTokenDto dto);
    String getUserAccount(Long userId);
    String getUserToken(Long userId);

}
