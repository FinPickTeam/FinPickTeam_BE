package org.scoula.finance.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.scoula.finance.dto.DepositDetailDto;

import java.util.List;

@Mapper
public interface DepositMapper {
    List<DepositDetailDto> selectAllDeposits();
}
