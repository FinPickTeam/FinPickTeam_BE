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
import java.util.Map;
import java.util.*;

import static java.util.Map.entry;

@Slf4j
@Service
@RequiredArgsConstructor
public class NhCardServiceImpl implements NhCardService {

    private final NHApiClient nhApiClient;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int CARD_DUMMY_MONTHS_BACK = 18; // 1.5년치
    private static final int CARD_MIN_DAILY = 1;
    private static final int CARD_MAX_DAILY = 12;

    // callCardTransactionList 내부: 더미 사용 지점만 변경
    @Override
    public List<NhCardTransactionResponseDto> callCardTransactionList(Long userId, String finCard, String from, String to) {
        List<NhCardTransactionResponseDto> all = new ArrayList<>();
        int page = 1;

        while (true) {
            JSONObject res = nhApiClient.callCardTransactionList(finCard, from, to, page);
            String rpcd = res.getJSONObject("Header").optString("Rpcd", "");

            if ("A0090".equals(rpcd)) {
                log.info("✅ 카드 승인내역 없음 → 대용량 더미 생성 (핀카드: {})", finCard);
                return createCardDummyTransactions(userId, finCard);
            }
            if (!"00000".equals(rpcd)) {
                throw new NHApiException("카드 승인내역 조회 실패: " + rpcd);
            }

            List<NhCardTransactionResponseDto> parsed = NhCardParser.parse(res);
            all.addAll(parsed);

            if (!"Y".equalsIgnoreCase(res.optString("CtntDataYn"))) break;
            page++;
        }

        // 실제가 너무 적으면(예: 개발계) 부족분을 더미로 보강하고 싶다면 아래 주석 해제
        // if (all.size() < 500) {
        //     all.addAll(createCardDummyTransactions(userId, finCard));
        // }

        return all.isEmpty() ? createCardDummyTransactions(userId, finCard) : all;
    }


    // 기존 createDummyTransactions() 완전 교체

    private List<NhCardTransactionResponseDto> createCardDummyTransactions(Long userId, String finCard) {
        List<NhCardTransactionResponseDto> list = new ArrayList<>();

        String[][] merchants = {
                {"스타벅스","커피전문점"}, {"맥도날드","패스트푸드"}, {"올리브영","화장품"},
                {"GS25","편의점"}, {"넷플릭스","정기결제"}, {"카카오모빌리티","택시"},
                {"이마트","마트"}, {"메가박스","영화관"}, {"LG유플러스","통신비"},
                {"삼성생명","보험"}, {"무신사","쇼핑"}, {"마켓컬리","식자재"},
                {"배달의민족","배달"}, {"쿠팡","쇼핑"}
        };
        Map<String,String> mcc = Map.ofEntries(
                entry("커피전문점","D101"),
                entry("패스트푸드","D102"),
                entry("화장품","D103"),
                entry("편의점","D104"),
                entry("정기결제","D105"),
                entry("택시","D106"),
                entry("마트","D107"),
                entry("영화관","D108"),
                entry("통신비","D109"),
                entry("보험","D110"),
                entry("쇼핑","D111"),
                entry("식자재","D112"),
                entry("배달","D113")
        );

        LocalDate end = LocalDate.now(KST);
        LocalDate start = end.minusMonths(CARD_DUMMY_MONTHS_BACK);
        long seed = Objects.hash(userId, finCard, start, end, "CARD_HEAVY");
        Random rnd = new Random(seed);

        String[] salesTypes = {"1","2","3","6","7","8"}; // enum 허용 값

        int globalIndex = 0;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            // 주중은 적당히, 주말과 금요일엔 더 많이
            int base = CARD_MIN_DAILY + rnd.nextInt(CARD_MAX_DAILY - CARD_MIN_DAILY + 1);
            if (d.getDayOfWeek() == DayOfWeek.FRIDAY || d.getDayOfWeek() == DayOfWeek.SATURDAY) {
                base += rnd.nextInt(4); // +0~3
            }

            for (int i = 0; i < base; i++) {
                String[] entry = merchants[rnd.nextInt(merchants.length)];
                String merchantName = entry[0];
                String tpbcdNm = entry[1];
                String tpbcd = mcc.getOrDefault(tpbcdNm, "D999");

                // 08:00~23:59 무작위 승인
                int hour = 8 + rnd.nextInt(16);
                int minute = rnd.nextInt(60);
                LocalDateTime approvedAt = d.atTime(hour, minute, rnd.nextInt(60));

                // 결제일(청구)은 보통 +2~+5일
                LocalDate paymentDate = d.plusDays(2 + rnd.nextInt(4));

                BigDecimal amount = BigDecimal.valueOf(3_000 + rnd.nextInt(250_000));
                boolean cancelled = rnd.nextInt(8) == 0; // 12.5% 취소
                BigDecimal cancelAmt = cancelled ? amount : BigDecimal.ZERO;

                String salesType = salesTypes[rnd.nextInt(salesTypes.length)];

                // 고유 승인번호 (결정적)
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
                        .installmentMonth(salesType.equals("2") ? (1 + rnd.nextInt(12)) : 0)
                        .currency("KRW")
                        .foreignAmount(BigDecimal.ZERO)
                        .build());

                globalIndex++;
            }
        }

        return list;
    }

}
