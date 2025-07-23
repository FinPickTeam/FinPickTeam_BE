package org.scoula.finance.service;

import org.mapstruct.Mapper;
import org.scoula.finance.dto.DepositDetailDto;
import org.scoula.finance.dto.DepositFilterDto;
import org.scoula.finance.dto.DepositListDto;
import org.scoula.finance.dto.DepositRecommendationDto;

import java.util.List;
import java.util.Map;

public interface DepositService {
    //예금 전체 목록
    List<DepositListDto> getDeposits(DepositFilterDto filter);
    // 예금 상세 정보
    DepositDetailDto selectDepositByProductName(String depositProductName);
    // 예금 추천 로직
    List<Map<String, Object>> getAllDepositRecommendations(int amount, int period);
}
