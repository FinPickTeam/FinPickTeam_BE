package org.scoula.nhapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.scoula.nhapi.client.NHApiClient;
import org.scoula.nhapi.dto.FinAccountRequestDto;
import org.scoula.nhapi.dto.NhAccountTransactionResponseDto;
import org.scoula.nhapi.exception.NHApiException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NhAccountServiceImpl implements NhAccountService {

    private final NHApiClient nhApiClient;

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
        JSONObject res = nhApiClient.callTransactionList(finAcno, from, to);
        String rpcd = res.getJSONObject("Header").getString("Rpcd");

        if ("A0090".equals(rpcd)) {
            log.info("✅ 거래내역 없음 → 더미 생성 (finAcno: {}, 기간: {}~{})", finAcno, from, to);
            return createDummyTransactions(userId, accountId, from, to);
        }
        if (!"00000".equals(rpcd)) {
            throw new NHApiException("거래내역 조회 실패: " + rpcd);
        }

        JSONArray arr = res.optJSONArray("Rec");
        List<NhAccountTransactionResponseDto> list = new ArrayList<>();
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                // DTO 오버로드 버전이 있으면 from(obj, userId, accountId) 쓰세요.
                NhAccountTransactionResponseDto dto = NhAccountTransactionResponseDto.from(arr.getJSONObject(i));
                dto.setUserId(userId);
                dto.setAccountId(accountId);
                list.add(dto);
            }
        }

        if (list.isEmpty()) {
            log.info("ℹ️ NH 응답은 성공이지만 기록 0건 → 더미 생성 (finAcno: {}, 기간: {}~{})", finAcno, from, to);
            return createDummyTransactions(userId, accountId, from, to);
        }
        return list;
    }

    /**
     * 고급 더미 생성기
     * - 기간(from~to, yyyyMMdd) 엄수
     * - 유저ID+계좌ID 기반 시드 → 계좌가 달라도 일관/독립 생성
     * - 월급/고정비/일상소비 패턴 섞기
     * - 금액/가게명/메모 다양화
     * - tuNo: (accountId, 날짜, 인덱스) 기반 결정적 키 → 중복 방지
     */
    private List<NhAccountTransactionResponseDto> createDummyTransactions(Long userId, Long accountId, String from, String to) {
        LocalDate start = parseYYYYMMDD(from, LocalDate.now().minusMonths(3));
        LocalDate end   = parseYYYYMMDD(to,   LocalDate.now());

        // 시드: 유저+계좌 고정 (계좌마다 다르고, 재호출해도 동일 패턴)
        long seed = Objects.hash(userId, accountId, start, end);
        Random rnd = new Random(seed);

        // 초기 잔액도 시드 기반으로 약간씩 다르게
        BigDecimal balance = BigDecimal.valueOf(700_000 + rnd.nextInt(600_000));

        // 레퍼런스 테이블
        List<String> shops = List.of("스타벅스", "이마트24", "GS25", "파리바게뜨", "배민", "쿠팡", "요기요", "다이소", "교보문고", "롯데마트");
        List<String> memos = List.of("점심", "출근 커피", "택시", "휴지/생필품", "저녁 배달", "간식", "주말 장보기", "선물", "구독료", "쿠폰사용");

        // 월급/고정비 설정
        int payday = 25;                       // 월급일
        BigDecimal salary = BigDecimal.valueOf(2_000_000 + rnd.nextInt(600_000));
        Map<Integer, BigDecimal> fixedBills = Map.of(
                2, BigDecimal.valueOf(49_000),    // 통신비
                8, BigDecimal.valueOf(12_900),    // 넷플릭스(예시)
                15, BigDecimal.valueOf(90_000)    // 관리비
        );

        List<NhAccountTransactionResponseDto> out = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            // 1) 월급 (payday, 주말이면 전 영업일로 조정)
            LocalDate salaryDate = adjustToWeekday(d.withDayOfMonth(Math.min(payday, d.lengthOfMonth())));
            if (d.equals(salaryDate)) {
                balance = balance.add(salary);
                out.add(buildDto(userId, accountId, d.atTime(9, rnd.nextInt(20)), true,
                        salary, balance, "급여", "월급",
                        makeTuNo(accountId, d, 0)));
            }

            // 2) 고정비
            for (Map.Entry<Integer, BigDecimal> bill : fixedBills.entrySet()) {
                if (d.getDayOfMonth() == bill.getKey()) {
                    BigDecimal amt = bill.getValue();
                    balance = balance.subtract(amt);
                    String place = switch (bill.getKey()) {
                        case 2 -> "KT 통신요금";
                        case 8 -> "넷플릭스 구독";
                        case 15 -> "아파트 관리비";
                        default -> "고정비";
                    };
                    out.add(buildDto(userId, accountId, d.atTime(8, rnd.nextInt(20)), false,
                            amt, balance, place, "자동이체",
                            makeTuNo(accountId, d, bill.getKey())));
                }
            }

            // 3) 일상 소비 (평균 0~2건/일)
            int dailyCnt = rnd.nextInt(3); // 0,1,2개
            for (int i = 0; i < dailyCnt; i++) {
                boolean incomeChance = rnd.nextInt(20) == 0; // 가끔 환불/입금
                boolean isIncome = incomeChance;

                BigDecimal amt = isIncome
                        ? BigDecimal.valueOf(10_000 + rnd.nextInt(40_000))
                        : BigDecimal.valueOf(3_000 + rnd.nextInt(70_000));

                balance = isIncome ? balance.add(amt) : balance.subtract(amt);

                String place = isIncome ? "이체입금" : shops.get(rnd.nextInt(shops.size()));
                String memo  = memos.get(rnd.nextInt(memos.size()));

                out.add(buildDto(userId, accountId,
                        d.atTime(10 + rnd.nextInt(10), rnd.nextInt(60)),
                        isIncome, amt, balance, place, memo,
                        makeTuNo(accountId, d, 100 + i)));
            }
        }

        // 기간 내에서 랜덤이지만 재현 가능한 순서 보장
        out.sort(Comparator.comparing(NhAccountTransactionResponseDto::getDate));
        return out;
    }

    private NhAccountTransactionResponseDto buildDto(
            Long userId, Long accountId, LocalDateTime when, boolean income,
            BigDecimal amount, BigDecimal balance, String place, String memo, long tuNo
    ) {
        return NhAccountTransactionResponseDto.builder()
                .userId(userId)
                .accountId(accountId)
                .date(when)
                .type(income ? "INCOME" : "EXPENSE")
                .amount(amount)
                .balance(balance)
                .place(place)
                .isCancelled(false)
                .tuNo(tuNo)
                .memo(memo)
                .category(null)
                .analysis(null)
                .build();
    }

    private static LocalDate adjustToWeekday(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY) return date.minusDays(1);
        if (dow == DayOfWeek.SUNDAY)   return date.minusDays(2);
        return date;
    }

    private static LocalDate parseYYYYMMDD(String yyyymmdd, LocalDate fallback) {
        try {
            return LocalDate.parse(yyyymmdd, DateTimeFormatter.BASIC_ISO_DATE);
        } catch (Exception e) {
            return fallback;
        }
    }

    private static long makeTuNo(Long accountId, LocalDate date, int index) {
        // 계좌 분리 + 날짜 + 인덱스 → 결정적 키 (증분 동기화/중복체크에 안전)
        long a = (accountId == null) ? 0L : accountId;
        long d = Long.parseLong(date.format(DateTimeFormatter.BASIC_ISO_DATE));
        long h = 1125899906842597L;
        h = 31*h + a;
        h = 31*h + d;
        h = 31*h + index;
        return Math.abs(h);
    }
}
