package org.scoula.nhapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.scoula.account.domain.Account;
import org.scoula.account.mapper.AccountMapper;
import org.scoula.nhapi.client.NHApiClient;
import org.scoula.nhapi.dto.FinAccountRequestDto;
import org.scoula.nhapi.dto.NhAccountTransactionResponseDto;
import org.scoula.nhapi.exception.NHApiException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NhAccountServiceImpl implements NhAccountService {

    private final NHApiClient nhApiClient;
    private final AccountMapper accountMapper; // ★ 추가

    @Override
    public String callOpenFinAccount(FinAccountRequestDto dto) {
        dto.validate();

        JSONObject res = nhApiClient.callOpenFinAccount(dto.getAccountNumber(), dto.getBirthday());
        log.info("🔍 핀어카운트 발급 응답: {}", res);

        String rpcd = res.getJSONObject("Header").getString("Rpcd");
        if ("A0013".equals(rpcd)) throw new NHApiException("이미 등록된 핀어카운트입니다.");
        if (!"00000".equals(rpcd)) throw new NHApiException("핀어카운트 발급 실패: " + rpcd);

        String rgno = res.optString("Rgno", null);
        if (rgno == null) throw new NHApiException("Rgno가 응답에 없습니다.");

        for (int attempt = 1; attempt <= 3; attempt++) {
            JSONObject checkRes = nhApiClient.callCheckFinAccount(rgno, dto.getBirthday());
            log.info("🔁 [{}] 핀어카운트 확인 응답: {}", attempt, checkRes);

            if ("00000".equals(checkRes.getJSONObject("Header").getString("Rpcd"))) {
                String finAcno = checkRes.optString("FinAcno", null);
                if (finAcno != null) return finAcno;
            }
            try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        throw new NHApiException("핀어카운트 확인 실패 (FinAcno 누락)");
    }

    @Override
    public BigDecimal callInquireBalance(String finAcno) {
        JSONObject res = nhApiClient.callInquireBalance(finAcno);
        String rpcd = res.getJSONObject("Header").getString("Rpcd");
        if (!"00000".equals(rpcd)) throw new NHApiException("잔액 조회 실패: " + rpcd);
        return new BigDecimal(res.getString("Ldbl"));
    }

    @Override
    public List<NhAccountTransactionResponseDto> callTransactionList(
            Long userId, Long accountId, String finAcno, String from, String to
    ) {
        // ★ 계좌 타입 조회
        Account acc = accountMapper.findById(accountId);
        String accType = (acc != null && acc.getAccountType() != null) ? acc.getAccountType() : "DEPOSIT";
        if (!"DEPOSIT".equalsIgnoreCase(accType)) {
            log.info("↩️ {} 계좌 거래 스킵 (accountId={})", accType, accountId);
            return java.util.Collections.emptyList();
        }

        JSONObject res = nhApiClient.callTransactionList(finAcno, from, to);
        String rpcd = res.getJSONObject("Header").getString("Rpcd");

        // NH가 "거래없음"을 줬을 때
        if ("A0090".equals(rpcd)) {
            log.info("✅ 거래내역 없음 (finAcno: {}, 기간: {}~{}, type={})", finAcno, from, to, accType);
            // ★ SAVING/INVEST는 더미 생성 금지
            if (!"DEPOSIT".equalsIgnoreCase(accType)) return Collections.emptyList();
            return createDummyTransactions(userId, accountId, from, to);
        }
        if (!"00000".equals(rpcd)) {
            throw new NHApiException("거래내역 조회 실패: " + rpcd);
        }

        // 키 대소문자 모두 대응
        JSONArray arr = res.optJSONArray("Rec");
        if (arr == null) arr = res.optJSONArray("REC");

        List<NhAccountTransactionResponseDto> list = new ArrayList<>();
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                NhAccountTransactionResponseDto dto = NhAccountTransactionResponseDto.from(arr.getJSONObject(i));
                dto.setUserId(userId);
                dto.setAccountId(accountId);
                list.add(dto);
            }
        }

        if (list.isEmpty()) {
            log.info("ℹ️ NH 성공이지만 기록 0건 (type={})", accType);
            // ★ SAVING/INVEST는 더미 생성 금지
            if (!"DEPOSIT".equalsIgnoreCase(accType)) return Collections.emptyList();
            return createDummyTransactions(userId, accountId, from, to);
        }
        return list;
    }

    /* ===== 아래는 네 기존 더미 생성 로직: DEPOSIT에서만 사용 ===== */

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int  DUMMY_MONTHS_BACK = 6;
    private static final int  MIN_DAILY = 0;
    private static final int  MAX_DAILY = 6;
    private static final int  PAYDAY = 25;

    private static final BigDecimal SAFETY = bd(10_000);
    private static final BigDecimal MIN_TX = bd(2_000);
    private static final BigDecimal MAX_TX = bd(180_000);
    private static final BigDecimal MIN_DAILY_BUDGET = bd(20_000);
    private static final BigDecimal MAX_DAILY_BUDGET = bd(300_000);

    private static final List<String> SHOPS = List.of(
            "스타벅스","이마트24","GS25","파리바게뜨","배달의민족",
            "쿠팡","요기요","다이소","교보문고","롯데마트","무신사","컬리","쿠팡이츠"
    );
    private static final List<String> MEMOS = List.of(
            "점심","출근 커피","택시","생필품","저녁 배달","간식",
            "주말 장보기","선물","구독료","쿠폰사용","회식","간단 장보기"
    );

    private static final class FixedBill {
        final int day; final BigDecimal amt; final String label;
        FixedBill(int day, long amt, String label) { this.day = day; this.amt = bd(amt); this.label = label; }
    }
    private static final List<FixedBill> FIXED_BILLS = List.of(
            new FixedBill(2,   49_000, "KT 통신요금"),
            new FixedBill(8,   12_900, "넷플릭스 구독"),
            new FixedBill(15,  90_000, "아파트 관리비"),
            new FixedBill(23,  19_800, "멜론 구독")
    );

    private List<NhAccountTransactionResponseDto> createDummyTransactions(
            Long userId, Long accountId, String from, String to
    ) {
        LocalDate today = LocalDate.now(KST);
        LocalDate end   = min(parseYYYYMMDD(to, today), today);
        LocalDate start = parseYYYYMMDD(from, end.minusMonths(DUMMY_MONTHS_BACK));
        if (start.isAfter(end)) start = end.minusDays(7);

        long baseSeed = Objects.hash(userId, accountId) * 0x9E3779B97F4A7C15L;
        BigDecimal balance = initialBalance(accountId);

        List<NhAccountTransactionResponseDto> out = new ArrayList<>();
        YearMonth curYm = null;
        BigDecimal monthVarRemain = BigDecimal.ZERO;
        int daysLeftInMonth = 0;
        LocalDate paydayDate = null;
        BigDecimal salaryAmt = BigDecimal.ZERO;

        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            YearMonth ym = YearMonth.from(d);
            if (!ym.equals(curYm)) {
                curYm = ym;
                long monthSeed = baseSeed ^ (ym.getYear() * 100 + ym.getMonthValue());
                SplittableRandom mr = new SplittableRandom(monthSeed);

                salaryAmt = bd(2_000_000 + mr.nextInt(1_500_000));
                paydayDate = adjustToWeekday(ym.atDay(Math.min(PAYDAY, ym.lengthOfMonth())));

                BigDecimal fixedSum = FIXED_BILLS.stream().map(fb -> fb.amt).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal percent = bd(55 + mr.nextInt(31));
                BigDecimal plannedVar = salaryAmt.multiply(percent).divide(bd(100))
                        .add(bd(100_000 + mr.nextInt(300_000)));

                BigDecimal availableThisMonth = balance.add(salaryAmt).subtract(fixedSum);
                if (availableThisMonth.signum() < 0) availableThisMonth = BigDecimal.ZERO;

                monthVarRemain = plannedVar.min(availableThisMonth);
                daysLeftInMonth = ym.lengthOfMonth() - d.getDayOfMonth() + 1;
            }

            SplittableRandom dr = new SplittableRandom(baseSeed ^ d.toEpochDay());

            if (d.equals(paydayDate)) {
                balance = balance.add(salaryAmt);
                out.add(buildDto(userId, accountId, randomTime(dr, d), true, salaryAmt, balance,
                        "급여", "월급", makeTuNo(accountId, d, 0)));
            }

            for (FixedBill fb : FIXED_BILLS) {
                if (d.getDayOfMonth() == fb.day) {
                    if (balance.compareTo(fb.amt) < 0) {
                        BigDecimal need = fb.amt.subtract(balance).add(SAFETY);
                        balance = balance.add(need);
                        out.add(buildDto(userId, accountId, randomTime(dr, d), true, need, balance,
                                "이체입금", "자동충전", makeTuNo(accountId, d, 7000 + fb.day)));
                    }
                    balance = balance.subtract(fb.amt);
                    out.add(buildDto(userId, accountId, randomTime(dr, d), false, fb.amt, balance,
                            fb.label, "자동이체", makeTuNo(accountId, d, fb.day)));
                }
            }

            int dailyCnt = MIN_DAILY + dr.nextInt(MAX_DAILY - MIN_DAILY + 1);
            if (d.getDayOfWeek() == DayOfWeek.FRIDAY || d.getDayOfWeek() == DayOfWeek.SATURDAY) {
                dailyCnt += dr.nextInt(3);
            }
            dailyCnt = Math.max(MIN_DAILY, Math.min(MAX_DAILY + 2, dailyCnt));

            BigDecimal todayTarget = daysLeftInMonth > 0
                    ? monthVarRemain.divide(bd(daysLeftInMonth), 0, RoundingMode.DOWN)
                    : BigDecimal.ZERO;

            BigDecimal minToday = max(MIN_DAILY_BUDGET, todayTarget.multiply(bd(60)).divide(bd(100)));
            BigDecimal maxToday = min(MAX_DAILY_BUDGET, todayTarget.multiply(bd(140)).divide(bd(100)));
            BigDecimal plannedToday = clamp(todayTarget, minToday, maxToday);

            BigDecimal spentToday = BigDecimal.ZERO;

            for (int i = 0; i < dailyCnt; i++) {
                BigDecimal remainGoal = plannedToday.subtract(spentToday);
                BigDecimal availToSpend = balance.subtract(SAFETY);
                BigDecimal remain = min(remainGoal, availToSpend);

                if (remain.compareTo(MIN_TX) < 0) break;

                BigDecimal cap = min(remain, MAX_TX);
                int capInt = cap.intValue();
                if (capInt < MIN_TX.intValue()) break;

                BigDecimal amt = MIN_TX.add(bd(new SplittableRandom(baseSeed ^ (d.toEpochDay() + i))
                        .nextInt(capInt - MIN_TX.intValue() + 1)));

                boolean isIncome = new SplittableRandom(baseSeed ^ (d.toEpochDay() * 13 + i)).nextInt(22) == 0;
                LocalDateTime when = randomTime(dr, d);

                if (isIncome) {
                    balance = balance.add(amt);
                    out.add(buildDto(userId, accountId, when, true, amt, balance,
                            "이체입금", MEMOS.get(dr.nextInt(MEMOS.size())), makeTuNo(accountId, d, 1000 + i)));
                } else {
                    if (balance.subtract(amt).compareTo(SAFETY) < 0) break;
                    balance = balance.subtract(amt);
                    spentToday = spentToday.add(amt);
                    out.add(buildDto(userId, accountId, when, false, amt, balance,
                            SHOPS.get(dr.nextInt(SHOPS.size())), MEMOS.get(dr.nextInt(MEMOS.size())),
                            makeTuNo(accountId, d, 1000 + i)));
                }
            }

            monthVarRemain = max(BigDecimal.ZERO, monthVarRemain.subtract(spentToday));
            daysLeftInMonth--;
        }

        out.sort(Comparator.comparing(NhAccountTransactionResponseDto::getDate));
        return out;
    }

    // ===== Helper (그대로) =====
    private static BigDecimal bd(long v) { return BigDecimal.valueOf(v); }
    private static BigDecimal bd(int v)  { return BigDecimal.valueOf(v); }
    private static BigDecimal max(BigDecimal a, BigDecimal b){ return a.compareTo(b) >= 0 ? a : b; }
    private static BigDecimal min(BigDecimal a, BigDecimal b){ return a.compareTo(b) <= 0 ? a : b; }
    private static BigDecimal clamp(BigDecimal v, BigDecimal lo, BigDecimal hi){ return max(lo, min(hi, v)); }
    private static LocalDate min(LocalDate a, LocalDate b){ return a.isBefore(b) ? a : b; }
    private static LocalDate adjustToWeekday(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY) return date.minusDays(1);
        if (dow == DayOfWeek.SUNDAY)   return date.minusDays(2);
        return date;
    }
    private NhAccountTransactionResponseDto buildDto(Long userId, Long accountId, LocalDateTime when, boolean income,
                                                     BigDecimal amount, BigDecimal balance, String place, String memo, long tuNo) {
        return NhAccountTransactionResponseDto.builder()
                .userId(userId).accountId(accountId).date(when)
                .type(income ? "INCOME" : "EXPENSE")
                .amount(amount).balance(balance).place(place)
                .isCancelled(false).tuNo(tuNo).memo(memo)
                .category(null).analysis(null).build();
    }
    private static LocalDate parseYYYYMMDD(String yyyymmdd, LocalDate fallback) {
        try { return LocalDate.parse(yyyymmdd, DateTimeFormatter.BASIC_ISO_DATE); }
        catch (Exception e) { return fallback; }
    }
    private static long makeTuNo(Long accountId, LocalDate date, int index) {
        long a = (accountId == null) ? 0L : accountId;
        long d = date.toEpochDay();
        long h = 1125899906842597L;
        h = 31*h + a; h = 31*h + d; h = 31*h + index;
        return Math.abs(h);
    }
    private static LocalDateTime randomTime(SplittableRandom r, LocalDate d) {
        int hour = 9 + r.nextInt(13); // 09~21시
        int min  = r.nextInt(60);
        return d.atTime(hour, min);
    }
    private static BigDecimal initialBalance(Long accountId) {
        long h = Math.abs(Objects.hashCode(accountId));
        return bd(800_000 + (h % 2_000_000));
    }
}
