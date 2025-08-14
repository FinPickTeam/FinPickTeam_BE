package org.scoula.coin.service;

import lombok.RequiredArgsConstructor;
import org.scoula.coin.dto.CoinMonthlyResponseDTO;
import org.scoula.coin.dto.CoinStatusResponseDTO;
import org.scoula.coin.event.CoinChangedEvent;
import org.scoula.coin.mapper.CoinMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class CoinServiceImpl implements CoinService {

    private final CoinMapper coinMapper;
    private final ApplicationEventPublisher eventPublisher;

    /** 코인을 일반 증가(월누적 포함) - 기존 경로에서 사용 중인 메서드가 있다면 거기서 호출 */
    @Transactional
    public void addCoinIncludingMonthly(Long userId, int amount) {
        coinMapper.addCoinAmount(userId, amount);
        // 트랜잭션 커밋 후 실행되도록 이벤트 발행
        eventPublisher.publishEvent(new CoinChangedEvent(userId, true));
    }

    /** 코인을 월누적 제외 증가(축하금 등) */
    @Transactional
    public void addCoinExceptMonthly(Long userId, int amount) {
        coinMapper.addCoinAmountExceptMonthly(userId, amount);
        // 월누적에 반영되지 않으므로 굳이 랭킹 갱신 불필요
        eventPublisher.publishEvent(new CoinChangedEvent(userId, false));
    }

    /** 코인 차감 (월누적도 함께 줄이는 경우에만 true로 발행) */
    @Transactional
    public void subtractCoin(Long userId, int amount, boolean affectMonthly) {
        coinMapper.subtractCoin(userId, amount);
        if (affectMonthly) {
            // 월누적에도 반영하는 로직이 있다면 추가로 monthly 감소 쿼리 필요
            // coinMapper.subtractMonthlyCumulative(userId, amount); // 선택 구현
            eventPublisher.publishEvent(new CoinChangedEvent(userId, true));
        } else {
            eventPublisher.publishEvent(new CoinChangedEvent(userId, false));
        }
    }

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
