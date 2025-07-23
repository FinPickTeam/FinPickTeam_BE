package org.scoula.transactions.util;

import org.scoula.transactions.dto.TransactionDetailDTO;
import org.scoula.transactions.domain.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class RuleBasedAnalyzer {

    public static String analyze(TransactionDetailDTO dto, List<Transaction> recentTransactions) {
        String category = dto.getCategory();
        String place = dto.getPlace();
        BigDecimal amount = dto.getAmount();
        LocalDateTime date = dto.getDate();
        String memo = dto.getMemo();

        // 고액 식비
        if ("식비".equalsIgnoreCase(category) && amount.compareTo(new BigDecimal("100000")) > 0) {
            return "이번 식비는 10만원 이상으로 고액 소비입니다.\n지출 내역을 다시 확인해보세요.";
        }

        // 최근 7일간 식비 3회 이상
        long countCategory = recentTransactions.stream()
                .filter(t -> "식비".equalsIgnoreCase(t.getCategory()))
                .count();
        if ("식비".equalsIgnoreCase(category) && countCategory >= 3) {
            return "최근 일주일간 식비 거래가 3회 이상 발생했어요.\n식비 예산 점검이 필요합니다.";
        }

        // 같은 장소 2회 이상
        long samePlaceCount = recentTransactions.stream()
                .filter(t -> place.equalsIgnoreCase(t.getPlace()))
                .count();
        if (samePlaceCount >= 2) {
            return "같은 장소에서 반복 소비가 감지됐어요.\n패턴 소비가 아닌지 확인해보세요.";
        }

        // 야간 소비
        int hour = date.getHour();
        if (hour >= 22 || hour < 5) {
            return "늦은 밤 시간대의 소비입니다.\n충동 소비가 아니었는지 생각해보세요.";
        }

        // 고액 쇼핑
        if ("쇼핑".equalsIgnoreCase(category) && amount.compareTo(new BigDecimal("50000")) > 0) {
            return "이번 쇼핑은 고액 소비입니다.\n계획된 소비인지 점검해보세요.";
        }

        // 메모 없음
        if (memo == null || memo.trim().isEmpty()) {
            return "이 거래에는 메모가 없습니다.\n기록 습관을 들이면 분석에 도움이 돼요.";
        }

        return "이번 소비는 이상 없이 정상 범위로 판단돼요.\n좋은 소비 습관을 유지하고 있어요!";
    }
}
