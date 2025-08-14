package org.scoula.challenge.rank.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.challenge.rank.mapper.ChallengeCoinRankMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeCoinRankUpdateServiceImpl implements ChallengeCoinRankUpdateService {

    private final ChallengeCoinRankMapper rankMapper;

    private String currentMonth() {
        return LocalDate.now().withDayOfMonth(1).toString().substring(0, 7); // YYYY-MM
    }

    @Override
    public void updateUserAndRefreshMonthRanks(Long userId) {
        String month = currentMonth();

        // 1) 해당 유저의 당월 row upsert (coin.monthly_cumulative_amount + summary 기반)
        int up = rankMapper.upsertRankRowForUserCurrentMonth(userId, month);

        // 2) 당월 전체 rank 재계산 (윈도우 함수)
        int cnt = rankMapper.refreshRanksForMonth(month);

        log.info("[CoinRankUpdate] upsert user={}, month={}, up={}, refreshCnt={}", userId, month, up, cnt);
    }
}
