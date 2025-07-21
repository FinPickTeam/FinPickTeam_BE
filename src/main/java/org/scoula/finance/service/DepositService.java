package org.scoula.finance.service;

import org.scoula.finance.dto.DepositDetailDto;
import org.scoula.finance.dto.DepositRecommendationDto;

import java.util.List;

public interface DepositService {
    List<DepositDetailDto> getAllDepositDetails(); //예금 목록 받기
    List<DepositRecommendationDto> getAllDepositRecommendations(int userId, int amount, int period);
}
