package org.scoula.coin.service;

import lombok.RequiredArgsConstructor;
import org.scoula.coin.dto.CoinMonthlyResponseDTO;
import org.scoula.coin.dto.CoinStatusResponseDTO;
import org.scoula.coin.mapper.CoinMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class CoinServiceImpl implements CoinService {

    private final CoinMapper coinMapper;

    @Override
    public CoinMonthlyResponseDTO getMyMonthlyCoin(Long userId) {
        Long amount = coinMapper.getMonthlyCumulativeAmount(userId);
        String updatedAt = coinMapper.getUpdatedAt(userId);

        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        return CoinMonthlyResponseDTO.builder()
                .month(month)
                .amount(amount == null ? 0L : amount)
                .updatedAt(updatedAt)
                .build();
    }

    @Override
    public CoinStatusResponseDTO getMyCoinStatus(Long userId) {
        CoinStatusResponseDTO dto = coinMapper.getCoinStatus(userId);
        if (dto == null) {
            // coin row가 없다면 0으로 초기화 반환
            return CoinStatusResponseDTO.builder()
                    .amount(0).cumulativeAmount(0).monthlyCumulativeAmount(0)
                    .updatedAt(null).build();
        }
        return dto;
    }
}
