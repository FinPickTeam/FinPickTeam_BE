package org.scoula.nhapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.scoula.nhapi.client.NHApiClient;
import org.scoula.nhapi.dto.NhCardTransactionResponseDto;
import org.scoula.nhapi.exception.NHApiException;
import org.scoula.nhapi.parser.NhCardParser; // 이미 있다면 사용, 없으면 아래 주석 블록 참고
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NhCardServiceImpl implements NhCardService {

    private final NHApiClient nhApiClient;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int CARD_DUMMY_MONTHS_BACK = 18; // 1.5년치
    private static final int CARD_MIN_DAILY = 1;
    private static final int CARD_MAX_DAILY = 12;

    @Override
    public List<NhCardTransactionResponseDto> callCardTransactionList(
            Long userId, String finCard, String from, String to
    ) {
        List<NhCardTransactionResponseDto> all = new ArrayList<>();
        int page = 1;

        while (true) {
            JSONObject res = nhApiClient.callCardTransactionList(finCard, from, to, page);
            String rpcd = res.getJSONObject("Header").optString("Rpcd", "");

            if ("A0090".equals(rpcd)) {
                log.info("✅ 카드 승인내역 없음 → 더미 반환 (핀카드: {})", finCard);
                return createCardDummyTransactions(userId, finCard);
            }
            if (!"00000".equals(rpcd)) {
                throw new NHApiException("카드 승인내역 조회 실패: " + rpcd);
            }

            List<NhCardTransactionResponseDto> parsed = NhCardParser.parse(res); // 파서 있으면 사용
            all.addAll(parsed);

            if (!"Y".equalsIgnoreCase(res.optString("CtntDataYn"))) break;
            page++;
        }
        return all.isEmpty() ? createCardDummyTransactions(userId, finCard) : all;
    }



    private List<NhCardTransactionResponseDto> createCardDummyTransactions(Long userId, String finCard) {
        List<NhCardTransactionResponseDto> list = new ArrayList<>();

        String[][] merchants = {
                {"스타벅스","커피전문점"}, {"맥도날드","패스트푸드"}, {"올리브영","화장품"},
                {"GS25","편의점"}, {"넷플릭스","정기결제"}, {"카카오모빌리티","택시"},
                {"이마트","마트"}, {"메가박스","영화관"}, {"LG유플러스","통신비"},
                {"삼성생명","보험"}, {"무신사","쇼핑"}, {"마켓컬리","식자재"},
                {"배달의민족","배달"}, {"쿠팡","쇼핑"}
        };

        Map<String, String> mcc = new HashMap<String, String>();
        mcc.put("커피전문점","D101"); mcc.put("패스트푸드","D102"); mcc.put("화장품","D103");
        mcc.put("편의점","D104"); mcc.put("정기결제","D105"); mcc.put("택시","D106");
        mcc.put("마트","D107"); mcc.put("영화관","D108"); mcc.put("통신비","D109");
        mcc.put("보험","D110"); mcc.put("쇼핑","D111"); mcc.put("식자재","D112");
        mcc.put("배달","D113");

        LocalDate end = LocalDate.now(KST);
        LocalDate start = end.minusMonths(CARD_DUMMY_MONTHS_BACK);

        long seed = Math.abs(Objects.hash(userId, finCard, start, end, "CARD_HEAVY"));
        Random rnd = new Random(seed);
        String[] salesTypes = {"1","2","3","6","7","8"};

        long globalIndex = 0;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            int base = CARD_MIN_DAILY + rnd.nextInt(CARD_MAX_DAILY - CARD_MIN_DAILY + 1);
            DayOfWeek dow = d.getDayOfWeek();
            if (dow == DayOfWeek.FRIDAY || dow == DayOfWeek.SATURDAY) {
                base += rnd.nextInt(4); // 금/토 증량
            }

            for (int i = 0; i < base; i++) {
                String[] entry = merchants[rnd.nextInt(merchants.length)];
                String merchantName = entry[0];
                String tpbcdNm = entry[1];
                String tpbcd = mcc.containsKey(tpbcdNm) ? mcc.get(tpbcdNm) : "D999";

                int hour = 8 + rnd.nextInt(16); // 08~23
                int minute = rnd.nextInt(60);
                int second = rnd.nextInt(60);
                LocalDateTime approvedAt = d.atTime(hour, minute, second);
                LocalDate paymentDate = d.plusDays(2 + rnd.nextInt(4));

                BigDecimal amount = BigDecimal.valueOf(3000 + rnd.nextInt(250000));
                boolean cancelled = rnd.nextInt(8) == 0; // 12.5%
                BigDecimal cancelAmt = cancelled ? amount : BigDecimal.ZERO;

                String salesType = salesTypes[rnd.nextInt(salesTypes.length)];

                long authSeq = Math.abs(31L * approvedAt.toEpochSecond(ZoneOffset.ofHours(9)) + globalIndex);
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
                        .installmentMonth("2".equals(salesType) ? (1 + rnd.nextInt(12)) : 0)
                        .currency("KRW")
                        .foreignAmount(BigDecimal.ZERO)
                        .build());
                globalIndex++;
            }
        }
        return list;
    }

}
