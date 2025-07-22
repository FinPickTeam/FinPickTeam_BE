package org.scoula.finance.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.finance.dto.DepositDetailDto;
import org.scoula.finance.dto.DepositFilterDto;
import org.scoula.finance.dto.DepositListDto;
import org.scoula.finance.dto.DepositRecommendationDto;

import java.util.List;

@Mapper
public interface DepositMapper {
    List<DepositListDto> selectAllDeposits();
    List<DepositListDto> selectDepositsWithFilter(DepositFilterDto filterDto);
    List<DepositRecommendationDto> selectAllDepositRecommendations();
    DepositDetailDto selectDepositByProductName(@Param("productName") String productName);
    List<DepositRecommendationDto> selectDepositsByProductName(@Param("productName") String productName);

}
