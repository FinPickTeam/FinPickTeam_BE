package org.scoula.finance.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.finance.dto.DepositDetailDto;
import org.scoula.finance.dto.DepositRecommendationDto;

import java.util.List;

@Mapper
public interface DepositMapper {
    List<DepositDetailDto> selectAllDeposits();
    List<DepositRecommendationDto> selectAllDepositRecommendations();
    List<DepositRecommendationDto> selectDepositByProductName(@Param("productName") String productName);

}
