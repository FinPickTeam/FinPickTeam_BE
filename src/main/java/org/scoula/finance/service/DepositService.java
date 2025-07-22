package org.scoula.finance.service;

import org.mapstruct.Mapper;
import org.scoula.finance.dto.DepositDetailDto;
import org.scoula.finance.dto.DepositRecommendationDto;

import java.util.List;
import java.util.Map;

public interface DepositService {
    List<DepositDetailDto> getAllDepositDetails(); //예금 목록 받기
    List<Map<String, Object>> getAllDepositRecommendations(int amount, int period);
}
