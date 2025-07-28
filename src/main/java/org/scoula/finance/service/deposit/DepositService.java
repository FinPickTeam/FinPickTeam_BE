package org.scoula.finance.service.deposit;

import org.scoula.finance.dto.deposit.DepositDetailDto;
import org.scoula.finance.dto.deposit.DepositFilterDto;
import org.scoula.finance.dto.deposit.DepositListDto;

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
