package org.scoula.finance.service.deposit;

import org.scoula.finance.dto.deposit.DepositDetailDto;
import org.scoula.finance.dto.deposit.DepositFilterDto;
import org.scoula.finance.dto.deposit.DepositListDto;
import org.scoula.finance.dto.deposit.DepositUserConditionDto;

import java.util.List;

public interface DepositService {
    //예금 전체 목록
    List<DepositListDto> getDeposits(DepositFilterDto filter);
    // 예금 상세 정보
    DepositDetailDto selectDepositByProductName(String depositProductName);
    // 예금 추천 로직
    List<DepositListDto> getAllDepositRecommendations(int amount, int period, DepositUserConditionDto userCondition);
}
