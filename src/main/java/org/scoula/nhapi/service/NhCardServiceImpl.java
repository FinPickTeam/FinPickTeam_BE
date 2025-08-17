package org.scoula.nhapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.scoula.nhapi.client.NHApiClient;
import org.scoula.nhapi.dto.NhCardTransactionResponseDto;
import org.scoula.nhapi.exception.NHApiException;
import org.scoula.nhapi.parser.NhCardParser;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static java.util.Map.entry;

@Slf4j
@Service
@RequiredArgsConstructor
public class NhCardServiceImpl implements NhCardService {

    private final NHApiClient nhApiClient;

    @Override
    public List<NhCardTransactionResponseDto> callCardTransactionList(Long userId, String finCard, String from, String to) {
        List<NhCardTransactionResponseDto> all = new ArrayList<>();
        int page = 1;

        while (true) {
            JSONObject res = nhApiClient.callCardTransactionList(finCard, from, to, page);
            JSONObject header = res.optJSONObject("Header");
            String rpcd = header != null ? header.optString("Rpcd", "") : res.optString("Rpcd", "");

            // ✅ A0090: 첫 페이지에서만 더미 리턴, 이후 페이지면 수집 종료(break)
            if ("A0090".equals(rpcd)) {
                if (page == 1 && all.isEmpty()) {
                    log.info("✅ 카드 승인내역 없음(1st page) → 더미 생성 (finCard: {}, {}~{})", finCard, from, to);
                    return createCardDummyTransactions(userId, finCard, from, to);
                } else {
                    log.info("ℹ️ 후속 페이지 A0090 → 페이징 종료 (수집건수: {})", all.size());
                    break;
                }
            }

            if (!"00000".equals(rpcd)) throw new NHApiException("카드 승인내역 조회 실패: " + rpcd);

            List<NhCardTransactionResponseDto> parsed = NhCardParser.parse(res);
            if (parsed != null && !parsed.isEmpty()) all.addAll(parsed);

            // ✅ CtntDataYn을 Header/루트 둘 다 체크
            String more = header != null ? header.optString("CtntDataYn", null) : null;
            if (more == null) more = res.optString("CtntDataYn", "N");
            if (!"Y".equalsIgnoreCase(more)) break;

            page++;
            if (page > 200) break; // 안전장치
        }

        return all.isEmpty() ? createCardDummyTransactions(userId, finCard, from, to) : all;
    }


    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int CARD_DUMMY_MONTHS_BACK = 6;
    private static final int CARD_MIN_DAILY = 0;
    private static final int CARD_MAX_DAILY = 6;          // ✅ 생성량 완화


    // ✅ 날짜별 고정 시드 → 기간 창이 달라도 동일 승인 생성(중복 방지)
    private List<NhCardTransactionResponseDto> createCardDummyTransactions(
            Long userId, String finCard, String from, String to
    ) {
        List<NhCardTransactionResponseDto> list = new ArrayList<>();

        String[][] merchants = {
                {"스타벅스","커피전문점"}, {"맥도날드","패스트푸드"}, {"올리브영","화장품"},
                {"GS25","편의점"}, {"넷플릭스","정기결제"}, {"카카오모빌리티","택시"},
                {"이마트","마트"}, {"메가박스","영화관"}, {"LG유플러스","통신비"},
                {"삼성생명","보험"}, {"무신사","쇼핑"}, {"마켓컬리","식자재"},
                {"배달의민족","배달"}, {"쿠팡","쇼핑"}
        };
        Map<String,String> mcc = Map.ofEntries(
                entry("커피전문점","D101"), entry("패스트푸드","D102"), entry("화장품","D103"),
                entry("편의점","D104"), entry("정기결제","D105"), entry("택시","D106"),
                entry("마트","D107"), entry("영화관","D108"), entry("통신비","D109"),
                entry("보험","D110"), entry("쇼핑","D111"), entry("식자재","D112"),
                entry("배달","D113")
        );

        // ✅ 기간 계산: to 없으면 오늘, from 없으면 6개월 전
        LocalDate end = (to != null && !to.isBlank())
                ? parseYYYYMMDD(to, LocalDate.now(KST))
                : LocalDate.now(KST);
        LocalDate start = (from != null && !from.isBlank())
                ? parseYYYYMMDD(from, end.minusMonths(CARD_DUMMY_MONTHS_BACK))
                : end.minusMonths(CARD_DUMMY_MONTHS_BACK);

        String[] salesTypes = {"1","2","3","6","7","8"};

        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            Random dayRnd = new Random(Objects.hash(userId, finCard, d)); // ✅ 날짜별 고정 시드(중복 방지)
            int base = CARD_MIN_DAILY + dayRnd.nextInt(CARD_MAX_DAILY - CARD_MIN_DAILY + 1);
            if (d.getDayOfWeek() == DayOfWeek.FRIDAY || d.getDayOfWeek() == DayOfWeek.SATURDAY) {
                base += dayRnd.nextInt(4);
            }

            for (int i = 0; i < base; i++) {
                String[] entry = merchants[dayRnd.nextInt(merchants.length)];
                String merchantName = entry[0];
                String tpbcdNm = entry[1];
                String tpbcd = mcc.getOrDefault(tpbcdNm, "D999");

                int hour = 8 + dayRnd.nextInt(16);
                int minute = dayRnd.nextInt(60);
                LocalDateTime approvedAt = d.atTime(hour, minute, dayRnd.nextInt(60));

                LocalDate paymentDate = d.plusDays(2 + dayRnd.nextInt(4));

                BigDecimal amount = BigDecimal.valueOf(3_000 + dayRnd.nextInt(250_000));
                boolean cancelled = dayRnd.nextInt(8) == 0;
                BigDecimal cancelAmt = cancelled ? amount : BigDecimal.ZERO;

                String salesType = salesTypes[dayRnd.nextInt(salesTypes.length)];

                long authSeq = Math.abs(31L * approvedAt.toEpochSecond(ZoneOffset.ofHours(9)) + i);
                String authNumber = "DUM" + authSeq;

                list.add(NhCardTransactionResponseDto.builder()
                        .authNumber(authNumber)
                        .salesType(salesType)
                        .approvedAt(approvedAt)
                        .paymentDate(paymentDate)
                        .amount(amount)
                        .cancelled(cancelled)
                        .cancelAmount(cancelAmt)
                        .cancelledAt(cancelled ? approvedAt.plusMinutes(5) : null)
                        .merchantName(merchantName)
                        .tpbcd(tpbcd)
                        .tpbcdNm(tpbcdNm)
                        .installmentMonth(salesType.equals("2") ? (1 + dayRnd.nextInt(12)) : 0)
                        .currency("KRW")
                        .foreignAmount(BigDecimal.ZERO)
                        .build());
            }
        }

        return list;
    }
    private static LocalDate parseYYYYMMDD(String yyyymmdd, LocalDate fallback) {
        try {
            return LocalDate.parse(yyyymmdd, java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        } catch (Exception e) {
            return fallback;
        }
    }

}
