package org.scoula.finance.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.finance.dto.deposit.DepositDetailDto;
import org.scoula.finance.dto.deposit.DepositFilterDto;
import org.scoula.finance.dto.deposit.DepositListDto;
import org.scoula.finance.dto.deposit.DepositRecommendationDto;

import java.util.List;

@Mapper
public interface DepositMapper {
    List<DepositListDto> getDepoistList(DepositFilterDto filterDto);
    List<DepositRecommendationDto> selectAllDepositRecommendations();
    DepositDetailDto selectDepositByProductName(@Param("productName") String productName);
    List<DepositListDto> selectDepositListByProductName(@Param("names") List<String> names);

}
