package org.scoula.transactions.util;

import org.scoula.transactions.domain.Ledger;
import org.scoula.transactions.domain.analysis.AnalysisCode;
import org.scoula.transactions.dto.AnalysisResult;
import org.scoula.transactions.service.AnalysisHelper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class AnalysisEngine {

    private final AnalysisHelper helper;
    private final List<AnalysisRule> RULES;

    // --------- 튜닝 가능한 임계치(운영에서 조절) ---------
    private static final BigDecimal HIGH_AMOUNT_ABS = BigDecimal.valueOf(100_000); // 10만원
    private static final BigDecimal HIGH_AMOUNT_INCOME_PCT = BigDecimal.valueOf(15); // 소득의 15% 이상
    private static final double     CAFE_RATIO_THRESH = 25.0; // 카페/간식 과다 비중
    private static final int        MART_7D_COUNT = 5;  // 최근 7일 마트/편의점 5회
    private static final String MART_LABEL = "편의점/마트/잡화";
    // -----------------------------------------------------

    // 할부 감지 정규식 (메모/비고에서 "할부", "N개월" 패턴 추출)
    private static final Pattern INSTALLMENT_PAT = Pattern.compile("(할부|\\b(\\d{1,2})\\s*개월\\b)");

    public AnalysisEngine(AnalysisHelper helper) {
        this.helper = helper;
        this.RULES = Arrays.asList(

                // 0) 할부 결제 (우선순위 가장 높음)
                new AnalysisRule(
                        AnalysisCode.INSTALLMENT,
                        (ledger, monthLedgers) -> isInstallment(ledger),
                        (ledger, monthLedgers) -> {
                            Integer months = extractInstallmentMonths(ledger);
                            String m = (months != null ? months + "개월" : "할부");
                            return "할부 결제(" + m + ")가 발생했어요. 고정 지출에 유의하세요.";
                        }
                ),

                // 1) 고액 결제: ①10만원↑ 또는 ②월 소득의 15%↑
                new AnalysisRule(
                        AnalysisCode.HIGH_AMOUNT,
                        (ledger, monthLedgers) -> isHighAmount(ledger),
                        (ledger, monthLedgers) -> {
                            YearMonth ym = YearMonth.from(ledger.getDate());
                            LocalDateTime from = ym.atDay(1).atStartOfDay();
                            LocalDateTime to = ym.plusMonths(1).atDay(1).atStartOfDay();
                            BigDecimal income = nvl(helper.getUserMonthlyIncomeByLedger(ledger.getUserId(), from, to));
                            String extra = "";
                            if (income.signum() > 0) {
                                BigDecimal pct = ledger.getAmount().multiply(BigDecimal.valueOf(100))
                                        .divide(income, 0, RoundingMode.HALF_UP);
                                extra = " (월소득의 " + pct + "%)";
                            }
                            return "10만 원 이상 지출이 발생했어요" + extra + ". 예산을 확인해보세요.";
                        }
                ),

                // 2) 카페/간식 과다 -> 중복 방지: '카테고리 최대금액 거래' 1건에만 부여
                new AnalysisRule(
                        AnalysisCode.CAFE_HEAVY_USER,
                        (ledger, monthLedgers) ->
                                isCategory("카페/간식", ledger) &&
                                        getCategoryRatio(monthLedgers, "카페/간식") >= CAFE_RATIO_THRESH &&
                                        isAnchorTxForCategoryMessage(monthLedgers, "카페/간식", ledger),
                        (ledger, monthLedgers) ->
                                String.format("카페/간식 카테고리에서 월 지출의 %.1f%%를 사용했어요.",
                                        getCategoryRatio(monthLedgers, "카페/간식"))
                ),

                // 3) 이번 달 최고액 결제
                new AnalysisRule(
                        AnalysisCode.MONTHLY_MAX,
                        (ledger, monthLedgers) -> ledger.getAmount().compareTo(getMaxAmount(monthLedgers)) == 0,
                        (ledger, monthLedgers) -> "이번 달 최고액 결제입니다! 큰 금액은 꼭 내역 점검!"
                ),

                // 4) 마트/편의점 1주 5회 이상 -> 정확히 5번째 거래 1건에만 코멘트
                new AnalysisRule(
                        AnalysisCode.MART_HABIT,
                        (ledger, monthLedgers) ->
                                isCategory(MART_LABEL, ledger) &&
                                        isNthOccurrenceWithinDays(monthLedgers, MART_LABEL, 7, MART_7D_COUNT, ledger),
                        (ledger, monthLedgers) ->
                                String.format("최근 7일간 편의점/마트 결제가 %d회! 습관적 소비 조심!", MART_7D_COUNT)
                ),

                // 5) 평소(3개월) 대비 업종 과소비
                new AnalysisRule(
                        AnalysisCode.CATEGORY_SURGE,
                        (ledger, monthLedgers) -> {
                            double prevAvg = helper.getCategoryPrevAvg(ledger.getUserId(), ledger.getCategory());
                            double now = getCategoryAmount(monthLedgers, ledger.getCategory());
                            return prevAvg > 0 && now > prevAvg * 1.5
                                    && isAnchorTxForCategoryMessage(monthLedgers, ledger.getCategory(), ledger);
                        },
                        (ledger, monthLedgers) -> {
                            double prevAvg = helper.getCategoryPrevAvg(ledger.getUserId(), ledger.getCategory());
                            double now = getCategoryAmount(monthLedgers, ledger.getCategory());
                            double diff = prevAvg > 0 ? ((now / prevAvg) - 1) * 100 : 0;
                            return String.format("이번 달 %s 지출이 평소보다 %.1f%% 더 많아요!", ledger.getCategory(), diff);
                        }
                ),

                // 6) 새 업종 첫 결제
                new AnalysisRule(
                        AnalysisCode.NEW_CATEGORY,
                        (ledger, monthLedgers) ->
                                !helper.hasUsedCategoryBefore(ledger.getUserId(), ledger.getCategory(), ledger.getDate()),
                        (ledger, monthLedgers) ->
                                String.format("이번 달 처음으로 %s 업종을 이용했어요! 새로운 경험이네요.", preferIndustryOrCategory(ledger))
                ),

                // 7) 심야 결제
                new AnalysisRule(
                        AnalysisCode.LATE_NIGHT,
                        (ledger, monthLedgers) -> {
                            int hour = ledger.getDate().getHour();
                            return hour >= 23 || hour < 5;
                        },
                        (ledger, monthLedgers) -> "심야(23~5시) 결제예요! 충동구매는 아닌지 한 번 더 체크!"
                ),

                // 8) 동일 가맹점 3회 -> 정확히 3번째 거래 1건만
                new AnalysisRule(
                        AnalysisCode.SAME_MERCHANT,
                        (ledger, monthLedgers) ->
                                getMerchantNth(monthLedgers, ledger.getMerchantName(), ledger) == 3,
                        (ledger, monthLedgers) ->
                                String.format("%s에서 한 달간 3회 결제! 단골이시네요 😄", safe(ledger.getMerchantName()))
                ),

                // 9) 업종(tpbcdNm) 코멘트 (있을 경우만)
                new AnalysisRule(
                        AnalysisCode.INDUSTRY_HINT,
                        (ledger, monthLedgers) -> hasIndustry(ledger),
                        (ledger, monthLedgers) -> industryMessage(preferIndustryOrCategory(ledger))
                ),

                // 10) 기본값
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
        // 수입은 분석하지 않음
        if (!"EXPENSE".equalsIgnoreCase(safe(ledger.getType()))) {
            return new AnalysisResult(AnalysisCode.NORMAL, ""); // 빈 메시지
        }

        for (AnalysisRule rule : RULES) {
            if (rule.getCondition().test(ledger, monthLedgers)) {
                String msg = rule.getMessageFunc().apply(ledger, monthLedgers);
                return new AnalysisResult(rule.getCode(), msg);
            }
        }
        return new AnalysisResult(AnalysisCode.NORMAL, "일반적인 소비 패턴입니다.");
    }

    // ==================== 헬퍼 (중복 방지/앵커 선정 등) ====================

    private static boolean isInstallment(Ledger l) {
        String memo = safe(l.getMemo());
        Matcher m = INSTALLMENT_PAT.matcher(memo);
        return m.find();
    }

    private static Integer extractInstallmentMonths(Ledger l) {
        String memo = safe(l.getMemo());
        Matcher m = INSTALLMENT_PAT.matcher(memo);
        if (m.find()) {
            try {
                // "12개월" 같은 케이스 우선 추출
                Matcher n = Pattern.compile("(\\d{1,2})\\s*개월").matcher(memo);
                if (n.find()) return Integer.parseInt(n.group(1));
            } catch (Exception ignore) {}
            return null; // '할부'만 적힌 경우
        }
        return null;
    }

    private boolean isHighAmount(Ledger l) {
        if (!"EXPENSE".equalsIgnoreCase(safe(l.getType()))) return false;
        if (l.getAmount().compareTo(HIGH_AMOUNT_ABS) >= 0) return true;

        // 월소득 대비 %
        YearMonth ym = YearMonth.from(l.getDate());
        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime to = ym.plusMonths(1).atDay(1).atStartOfDay();
        BigDecimal income = nvl(helper.getUserMonthlyIncomeByLedger(l.getUserId(), from, to));
        if (income.signum() <= 0) return false;

        BigDecimal pct = l.getAmount().multiply(BigDecimal.valueOf(100))
                .divide(income, 0, RoundingMode.HALF_UP);
        return pct.compareTo(HIGH_AMOUNT_INCOME_PCT) >= 0;
    }

    private static boolean isCategory(String categoryKo, Ledger l) {
        return categoryKo.equals(safe(l.getCategory()));
    }

    // 카테고리 과다/서지 메시지는 '그 카테고리의 월 최대 금액 거래'에게만 부여 (중복 방지)
    private static boolean isAnchorTxForCategoryMessage(List<Ledger> monthLedgers, String category, Ledger target) {
        BigDecimal max = monthLedgers.stream()
                .filter(l -> category.equals(l.getCategory()))
                .map(Ledger::getAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        // 동일 금액이 여러 건이면 가장 이른 거래에만 표기
        LocalDateTime firstTimeWithMax = monthLedgers.stream()
                .filter(l -> category.equals(l.getCategory()))
                .filter(l -> nvl(l.getAmount()).compareTo(max) == 0)
                .map(Ledger::getDate)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        return nvl(target.getAmount()).compareTo(max) == 0
                && target.getDate().equals(firstTimeWithMax);
    }

    // 동일 가맹점 N번째 거래인지 판단 (월 기준)
    private static int getMerchantNth(List<Ledger> monthLedgers, String merchant, Ledger target) {
        if (merchant == null) return -1;
        List<Ledger> sorted = monthLedgers.stream()
                .filter(l -> merchant.equals(l.getMerchantName()))
                .sorted(Comparator.comparing(Ledger::getDate))
                .collect(Collectors.toList());
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i) == target) return i + 1;
            // 동일 객체가 아닐 수 있어 날짜/금액/카테고리 동등성으로 보정
            Ledger x = sorted.get(i);
            if (x.getDate().equals(target.getDate())
                    && nvl(x.getAmount()).compareTo(nvl(target.getAmount())) == 0
                    && Objects.equals(safe(x.getCategory()), safe(target.getCategory()))) {
                return i + 1;
            }
        }
        return -1;
    }

    // 최근 days 범위에서 카테고리 n번째 거래인지
    private static boolean isNthOccurrenceWithinDays(List<Ledger> monthLedgers, String category, int days, int n, Ledger target) {
             LocalDateTime since = target.getDate().minusDays(days);
        List<Ledger> list = monthLedgers.stream()
                .filter(l -> category.equals(l.getCategory()))
                .filter(l -> l.getDate().isAfter(since))
                .sorted(Comparator.comparing(Ledger::getDate))
                .collect(Collectors.toList());
        if (list.size() < n) return false;
        Ledger nth = list.get(n - 1);
        return isSameTxn(nth, target);
    }

    private static boolean isSameTxn(Ledger a, Ledger b) {
        return a.getDate().equals(b.getDate())
                && nvl(a.getAmount()).compareTo(nvl(b.getAmount())) == 0
                && Objects.equals(safe(a.getMerchantName()), safe(b.getMerchantName()))
                && Objects.equals(safe(a.getCategory()), safe(b.getCategory()));
    }

    private static boolean hasIndustry(Ledger l) {
        // 카드 원천에서 내려주는 업종명/코드 필드가 있다면 사용
        // 도메인에 getIndustry() / getIndustryName() 중 있는 쪽으로 맞춰주세요.
        try {
            // 리플렉션 fallback (DB 변경 없이 카드 파이프라인에서만 set 가능)
            var m = l.getClass().getMethod("getIndustry");
            Object v = m.invoke(l);
            return v != null && !String.valueOf(v).isBlank();
        } catch (Exception ignore) {
            try {
                var m = l.getClass().getMethod("getIndustryName");
                Object v = m.invoke(l);
                return v != null && !String.valueOf(v).isBlank();
            } catch (Exception ignore2) {}
        }
        return false;
    }

    private static String preferIndustryOrCategory(Ledger l) {
        try {
            var m = l.getClass().getMethod("getIndustry");
            Object v = m.invoke(l);
            if (v != null && !String.valueOf(v).isBlank()) return String.valueOf(v);
        } catch (Exception ignore) {
            try {
                var m = l.getClass().getMethod("getIndustryName");
                Object v = m.invoke(l);
                if (v != null && !String.valueOf(v).isBlank()) return String.valueOf(v);
            } catch (Exception ignore2) {}
        }
        return safe(l.getCategory());
    }

    private static String industryMessage(String industry) {
        // 업종별 세밀 문구 템플릿 (필요시 확장)
        Map<String,String> t = Map.of(
                "의류/신발", "의류·신발 지출이 발생했어요. 시즌오프/아울렛을 활용해보세요.",
                "오락/문화", "오락·문화 지출이에요. 월 구독/패스 이용으로 비용을 나눠보세요.",
                "주거/수도/광열", "주거·수도·광열 비용입니다. 절약형 요금제/전기요금제 변경을 검토해보세요."
        );
        return t.getOrDefault(industry, industry + " 지출이에요. 예산 내에서 관리해보세요.");
    }

    // ---------- 기존 헬퍼들 (필요시 보강) ----------

    public static double getCategoryRatio(List<Ledger> ledgers, String category) {
        BigDecimal sum = ledgers.stream()
                .filter(l -> "EXPENSE".equalsIgnoreCase(safe(l.getType())))
                .filter(l -> category.equals(l.getCategory()))
                .map(Ledger::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = ledgers.stream()
                .filter(l -> "EXPENSE".equalsIgnoreCase(safe(l.getType())))
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

    private static String safe(String s) { return s == null ? "" : s; }
    private static BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
}
