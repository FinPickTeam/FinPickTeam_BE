package org.scoula.finance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.finance.dto.DepositDetailDto;
import org.scoula.finance.dto.DepositRecommendationDto;
import org.scoula.finance.mapper.DepositMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositServiceImpl implements DepositService {
    private final DepositMapper depositMapper;

    @Override
    public List<DepositDetailDto> getAllDepositDetails() {
        return depositMapper.selectAllDeposits();
    }

//    @Override
//    public List<DepositRecommendationDto> getAllDepositRecommendations(int userId, int amount, int period) {
//        return
//    }
}
