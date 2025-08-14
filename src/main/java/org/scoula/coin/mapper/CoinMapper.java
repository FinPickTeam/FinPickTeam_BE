package org.scoula.coin.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.coin.dto.CoinStatusResponseDTO;

public interface CoinMapper {
    void subtractCoin(@Param("userId") Long userId, @Param("amount") int amount);
    void addCoinAmount(@Param("userId") Long userId, @Param("amount") int amount);
    void insertCoinHistory(@Param("userId") Long userId,
                           @Param("amount") int amount,
                           @Param("type") String type,
                           @Param("coinType") String coinType);

    int getUserCoin(@Param("userId") Long userId);
    void insertInitialCoin(@Param("userId") Long userId);
    int getCumulativeAmount(@Param("userId") Long userId);

    Long getMonthlyCumulativeAmount(@Param("userId") Long userId);
    String getUpdatedAt(@Param("userId") Long userId);
    CoinStatusResponseDTO getCoinStatus(@Param("userId") Long userId);

    // 신규: 축하금 등 월누적 제외 증가용
    void addCoinAmountExceptMonthly(@Param("userId") Long userId, @Param("amount") int amount);

    // 신규: 월누적 전체 리셋
    void resetMonthlyCumulativeAll();
}
