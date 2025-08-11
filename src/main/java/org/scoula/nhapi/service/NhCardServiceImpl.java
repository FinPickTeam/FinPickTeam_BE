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
import java.util.*;

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
            String rpcd = res.getJSONObject("Header").optString("Rpcd", "");

            if ("A0090".equals(rpcd)) {
                log.info("✅ 카드 승인내역 없음 → 더미 반환 (핀카드: {})", finCard);
                return createDummyTransactions();
            }
            if (!"00000".equals(rpcd)) {
                throw new NHApiException("카드 승인내역 조회 실패: " + rpcd);
            }

            // 파서가 있으면 사용
            List<NhCardTransactionResponseDto> parsed = NhCardParser.parse(res);
            // 파서가 없다면 아래 주석 참고해서 간단 파싱 구현 가능
            // List<NhCardTransactionResponseDto> parsed = simpleParse(res);

            all.addAll(parsed);

            // 더보기 여부
            if (!"Y".equalsIgnoreCase(res.optString("CtntDataYn"))) break;
            page++;
        }

        return all.isEmpty() ? createDummyTransactions() : all;
    }

    // 필요 시 간단 파서 (NhCardParser 없으면 사용)
    /*
    private List<NhCardTransactionResponseDto> simpleParse(JSONObject res) {
        List<NhCardTransactionResponseDto> list = new ArrayList<>();
        var arr = res.optJSONArray("Rec");
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) {
            list.add(NhCardTransactionResponseDto.from(arr.getJSONObject(i)));
        }
        return list;
    }
    */

    // 샘플/데모용 더미 데이터
    private List<NhCardTransactionResponseDto> createDummyTransactions() {
        List<NhCardTransactionResponseDto> list = new ArrayList<>();

        String[][] merchants = {
                {"스타벅스", "커피전문점"}, {"맥도날드", "패스트푸드"}, {"올리브영", "화장품"},
                {"GS25", "편의점"}, {"넷플릭스", "정기결제"}, {"카카오모빌리티", "택시"},
                {"이마트", "마트"}, {"메가박스", "영화관"}, {"LG유플러스", "통신비"}, {"삼성생명", "보험"}
        };

        Map<String, String> mcc = Map.of(
                "커피전문점", "D101","패스트푸드", "D102","화장품", "D103","편의점", "D104",
                "정기결제", "D105","택시", "D106","마트", "D107","영화관", "D108",
                "통신비", "D109","보험", "D110"
        );

        LocalDate base = LocalDate.of(2025, 4, 1);
        int total = 40;

        for (int i = 0; i < total; i++) {
            int month = 4 + (i % 4); // 4~7월 분산
            int day = ((i * 3) % 27) + 1;
            int hour = 10 + (i * 5) % 10;
            int minute = (i * 7) % 60;

            LocalDateTime approvedAt = LocalDateTime.of(2025, month, day, hour, minute, 0);
            LocalDate paymentDate = LocalDate.of(2025, month, Math.min(day + 2, 28));

            String[] entry = merchants[i % merchants.length];
            String merchantName = entry[0];
            String tpbcdNm = entry[1];
            String tpbcd = mcc.getOrDefault(tpbcdNm, "D999");

            BigDecimal amount = BigDecimal.valueOf(3000 + (i * 1500 % 25000));
            boolean cancelled = (i % 6 == 0);

            list.add(NhCardTransactionResponseDto.builder()
                    .authNumber("AUTH" + (10000 + i))
                    .salesType(new String[]{"1","2","3","6","7","8"}[i % 6])
                    .approvedAt(approvedAt)
                    .paymentDate(paymentDate)
                    .amount(amount)
                    .cancelled(cancelled)
                    .cancelAmount(cancelled ? amount : BigDecimal.ZERO)
                    .cancelledAt(cancelled ? approvedAt.plusMinutes(5) : null)
                    .merchantName(merchantName)
                    .tpbcd(tpbcd)
                    .tpbcdNm(tpbcdNm)
                    .installmentMonth(0)
                    .currency("KRW")
                    .foreignAmount(BigDecimal.ZERO)
                    .build());
        }

        return list;
    }
}
