package org.scoula.coin.mapper;

import org.apache.ibatis.annotations.Param;

public interface CoinMapper {
    void subtractCoin(@Param("userId") Long userId, @Param("amount") int amount);
    void addCoinAmount(@Param("userId") Long userId, @Param("amount") int amount);
    void insertCoinHistory(@Param("userId") Long userId,
                           @Param("amount") int amount,
                           @Param("type") String type,
                           @Param("coinType") String coinType);

    int getUserCoin(@Param("userId") Long userId);

    // 회원가입시 초기 데이터 생성
    void insertInitialCoin(@Param("userId") Long userId);


}

