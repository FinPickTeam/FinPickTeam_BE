package org.scoula.transactions.util;

import org.scoula.transactions.domain.Ledger;

public class AnalysisEngine {

    public static String analyze(Ledger ledger/*, List<Ledger> monthLedgers */) {
        String feedback;

        if (ledger.getAmount().compareTo(new java.math.BigDecimal("10000")) < 0) {
            feedback = "소소한 생활 소비로 보입니다.\n누적되면 생각보다 큰 지출이 될 수 있어요.😅";
        }
        else if ("카페/간식".equals(ledger.getCategory())) {
            feedback = "카페/간식 소비가 자주 발생하고 있습니다.\n다음 달엔 음료/간식 예산을 정해보세요.";
        }
        else if ("식비".equals(ledger.getCategory()) || "외식".equals(ledger.getCategory())) {
            feedback = "외식/식비 소비가 눈에 띕니다.\n한 주 예산을 정해두면 통제에 도움이 돼요.";
        }
        else if ("이체".equals(ledger.getCategory())) {
            feedback = "이체/선물 지출이 확인됩니다.\n특별한 경우가 아니라면 자주 반복되지 않게 신경써보세요.";
        }
        else if (ledger.getAmount().compareTo(new java.math.BigDecimal("100000")) >= 0) {
            feedback = "고액 결제가 발생했습니다.\n지출 내역을 꼭 확인하고 계획적으로 소비하세요.";
        }
        else {
            feedback = "일반적인 소비 패턴입니다.\n계속해서 지출 내역을 점검해보세요.";
        }
        return feedback;
    }
}
