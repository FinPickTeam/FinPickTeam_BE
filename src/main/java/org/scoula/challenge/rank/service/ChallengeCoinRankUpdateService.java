package org.scoula.challenge.rank.service;

public interface ChallengeCoinRankUpdateService {
    void updateUserAndRefreshMonthRanks(Long userId);
}
