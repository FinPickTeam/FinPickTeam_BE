package org.scoula.transactions.util;

import org.scoula.transactions.domain.Ledger;
import org.scoula.transactions.domain.analysis.AnalysisCode;
import org.scoula.transactions.dto.AnalysisResult;
import org.scoula.transactions.service.AnalysisHelper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class AnalysisEngine {

    private final AnalysisHelper helper;
    private final List<AnalysisRule> RULES;

    // 생성자에서 RULES 초기화!
    public AnalysisEngine(AnalysisHelper helper) {
        this.helper = helper;
        this.RULES = Arrays.asList(
                // 카페/간식 과다
                new AnalysisRule(
                        AnalysisCode.CAFE_HEAVY_USER,
                        (ledger, monthLedgers) ->
                                "카페/간식".equals(ledger.getCategory()) && getCategoryRatio(monthLedgers, "카페/간식") >= 25.0,
                        (ledger, monthLedgers) ->
                                String.format("이번 달 카페/간식 소비가 전체의 %.1f%%입니다. 다음 달엔 예산을 세워보세요!", getCategoryRatio(monthLedgers, "카페/간식"))
                ),
                // 이번 달 최고액 결제
                new AnalysisRule(
                        AnalysisCode.MONTHLY_MAX,
                        (ledger, monthLedgers) ->
                                ledger.getAmount().compareTo(getMaxAmount(monthLedgers)) == 0,
                        (ledger, monthLedgers) ->
                                "이번 달 최고액 결제입니다! 큰 금액은 꼭 내역 점검!"
                ),
                // 마트/편의점 1주 5회 이상
                new AnalysisRule(
                        AnalysisCode.MART_HABIT,
                        (ledger, monthLedgers) ->
                                "마트".equals(ledger.getCategory()) && getMartCountThisWeek(monthLedgers) >= 5,
                        (ledger, monthLedgers) ->
                                String.format("최근 7일간 편의점/마트 결제가 %d회! 습관적 소비 조심!", getMartCountThisWeek(monthLedgers))
                ),
                // 평소(3개월) 대비 업종 과소비 (DB 평균값)
                new AnalysisRule(
                        AnalysisCode.CATEGORY_SURGE,
                        (ledger, monthLedgers) -> {
                            double prevAvg = this.helper.getCategoryPrevAvg(ledger.getUserId(), ledger.getCategory());
                            double now = getCategoryAmount(monthLedgers, ledger.getCategory());
                            return prevAvg > 0 && now > prevAvg * 1.5;
                        },
                        (ledger, monthLedgers) -> {
                            double prevAvg = this.helper.getCategoryPrevAvg(ledger.getUserId(), ledger.getCategory());
                            double now = getCategoryAmount(monthLedgers, ledger.getCategory());
                            double diff = prevAvg > 0 ? ((now / prevAvg) - 1) * 100 : 0;
                            return String.format("이번 달 %s 지출이 평소보다 %.1f%% 더 많아요!", ledger.getCategory(), diff);
                        }
                ),
                // 월 소득(실제 입금) 초과 소비
                new AnalysisRule(
                        AnalysisCode.INCOME_OVER,
                        (ledger, monthLedgers) -> {
                            LocalDateTime monthStart = ledger.getDate().withDayOfMonth(1).with(java.time.LocalTime.MIN);
                            LocalDateTime nextMonthStart = monthStart.plusMonths(1);
                            BigDecimal income = this.helper.getUserMonthlyIncomeByLedger(ledger.getUserId(), monthStart, nextMonthStart);
                            BigDecimal monthTotal = getMonthTotal(monthLedgers);
                            return income != null && income.compareTo(BigDecimal.ZERO) > 0 && monthTotal.compareTo(income) > 0;
                        },
                        (ledger, monthLedgers) -> {
                            LocalDateTime monthStart = ledger.getDate().withDayOfMonth(1).with(java.time.LocalTime.MIN);
                            LocalDateTime nextMonthStart = monthStart.plusMonths(1);
                            BigDecimal income = this.helper.getUserMonthlyIncomeByLedger(ledger.getUserId(), monthStart, nextMonthStart);
                            BigDecimal monthTotal = getMonthTotal(monthLedgers);
                            return String.format("이번 달 소비(%s원)가 계좌 입금액(%s원)을 초과했습니다! 내역 점검, 절약을 추천해요.",
                                    monthTotal.toPlainString(), income.toPlainString());
                        }
                ),
                // 새 업종 첫 결제 (DB)
                new AnalysisRule(
                        AnalysisCode.NEW_CATEGORY,
                        (ledger, monthLedgers) ->
                                !this.helper.hasUsedCategoryBefore(ledger.getUserId(), ledger.getCategory(), ledger.getDate()),
                        (ledger, monthLedgers) ->
                                String.format("이번 달 처음으로 %s 업종을 이용했어요! 새로운 경험이네요.", ledger.getCategory())
                ),
                // 심야 결제
                new AnalysisRule(
                        AnalysisCode.LATE_NIGHT,
                        (ledger, monthLedgers) -> {
                            int hour = ledger.getDate().getHour();
                            return hour >= 23 || hour < 5;
                        },
                        (ledger, monthLedgers) -> "심야(23~5시) 결제예요! 충동구매는 아닌지 한 번 더 체크!"
                ),
                // 동일 가맹점 3회 이상
                new AnalysisRule(
                        AnalysisCode.SAME_MERCHANT,
                        (ledger, monthLedgers) ->
                                getMerchantCount(monthLedgers, ledger.getMerchantName()) >= 3,
                        (ledger, monthLedgers) ->
                                String.format("%s에서 한 달간 %d회 결제! 단골이시네요 😄", ledger.getMerchantName(), getMerchantCount(monthLedgers, ledger.getMerchantName()))
                ),
                // 기본값
                new AnalysisRule(
                        AnalysisCode.NORMAL,
                        (ledger, monthLedgers) -> true,
                        (ledger, monthLedgers) -> "일반적인 소비 패턴입니다. 계속해서 지출 내역 점검해보세요."
                )
        );
    }

    /**
     * 메인 분석 진입점
     */
    public AnalysisResult analyze(Ledger ledger, List<Ledger> monthLedgers) {
        for (AnalysisRule rule : RULES) {
            if (rule.getCondition().test(ledger, monthLedgers)) {
                String msg = rule.getMessageFunc().apply(ledger, monthLedgers);
                return new AnalysisResult(rule.getCode(), msg);
            }
        }
        return new AnalysisResult(AnalysisCode.NORMAL, "일반적인 소비 패턴입니다.");
    }

    // ---------- 헬퍼 함수(금액, 빈도 등) ----------

    public static double getCategoryRatio(List<Ledger> ledgers, String category) {
        BigDecimal sum = ledgers.stream()
                .filter(l -> category.equals(l.getCategory()))
                .map(Ledger::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = ledgers.stream()
                .map(Ledger::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.compareTo(BigDecimal.ZERO) == 0 ? 0 :
                sum.divide(total, 2, RoundingMode.HALF_UP).doubleValue() * 100;
    }

    public static BigDecimal getMaxAmount(List<Ledger> ledgers) {
        return ledgers.stream()
                .map(Ledger::getAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    public static int getMartCountThisWeek(List<Ledger> ledgers) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        return (int) ledgers.stream()
                .filter(l -> "마트".equals(l.getCategory()))
                .filter(l -> l.getDate().isAfter(weekAgo))
                .count();
    }

    public static double getCategoryAmount(List<Ledger> ledgers, String category) {
        return ledgers.stream()
                .filter(l -> category.equals(l.getCategory()))
                .map(Ledger::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();
    }

    public static BigDecimal getMonthTotal(List<Ledger> ledgers) {
        return ledgers.stream()
                .map(Ledger::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static int getMerchantCount(List<Ledger> ledgers, String merchant) {
        if (merchant == null) return 0;
        return (int) ledgers.stream()
                .filter(l -> merchant.equals(l.getMerchantName()))
                .count();
    }
}
