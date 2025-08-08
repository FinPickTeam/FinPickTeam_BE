package org.scoula.transactions.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface AnalysisHelper {
    // 유저의 카테고리별 최근 3개월 평균 소비 금액
    double getCategoryPrevAvg(Long userId, String category);

    BigDecimal getUserMonthlyIncomeByLedger(Long userId, LocalDateTime monthStart, LocalDateTime nextMonthStart);

    // 해당 날짜 이전, 유저가 이 카테고리 쓴 적 있으면 true
    boolean hasUsedCategoryBefore(Long userId, String category, LocalDateTime before);

    // 유저가 최근 N개월간 가장 많이 쓴 업종(카테고리) Top N (랭킹)
    List<String> getTopCategories(Long userId, int monthCount);
}
