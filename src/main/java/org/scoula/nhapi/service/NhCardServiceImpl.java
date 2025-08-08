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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            JSONObject response = nhApiClient.callCardTransactionList(finCard, from, to, page);
            String rpcd = response.getJSONObject("Header").getString("Rpcd");

            if ("A0090".equals(rpcd)) {
                log.info("✅ 카드 승인내역 없음, 더미 데이터 반환 (핀카드: {})", finCard);
                return createDummyTransactions(); // 👈 더미 리턴
            }

            if (!"00000".equals(rpcd)) throw new NHApiException("카드 승인내역 실패");

            List<NhCardTransactionResponseDto> parsed = NhCardParser.parse(response);
            all.addAll(parsed);

            if (!"Y".equals(response.optString("CtntDataYn"))) break;
            page++;
        }

        return all.isEmpty() ? createDummyTransactions() : all;
    }

    private List<NhCardTransactionResponseDto> createDummyTransactions() {
        List<NhCardTransactionResponseDto> list = new ArrayList<>();

        String[][] merchants = {
                {"스타벅스", "커피전문점"}, {"맥도날드", "패스트푸드"}, {"올리브영", "화장품"},
                {"GS25", "편의점"}, {"넷플릭스", "정기결제"}, {"카카오모빌리티", "택시"},
                {"이마트", "마트"}, {"메가박스", "영화관"}, {"LG유플러스", "통신비"}, {"삼성생명", "보험"}
        };

        int dayCursor = 1;
        int total = 40;

        for (int i = 0; i < total; i++) {
            NhCardTransactionResponseDto dto = new NhCardTransactionResponseDto();

            // ✅ 월 분산: 4, 5, 6, 7월 균등 분포
            int month = 4 + (i % 4); // 4 ~ 7
            int day = (dayCursor++ % 28) + 1;
            if (i % 7 == 0) dayCursor++; // 날짜 살짝 섞기

            // ✅ 시간 랜덤
            int hour = 10 + (i * 3) % 9;
            int minute = (i * 7) % 60;

            // ✅ 승인일시 및 결제일자
            String date = "2025" + String.format("%02d", month) + String.format("%02d", day);
            String approvedAt = date + "T" + String.format("%02d%02d00", hour, minute);
            String paymentDate = "2025" + String.format("%02d", month) + String.format("%02d", Math.min(day + 2, 28));

            // ✅ 업종명 → 업종코드 맵
            Map<String, String> tpbcdMap = Map.of(
                    "커피전문점", "D101",
                    "패스트푸드", "D102",
                    "화장품", "D103",
                    "편의점", "D104",
                    "정기결제", "D105",
                    "택시", "D106",
                    "마트", "D107",
                    "영화관", "D108",
                    "통신비", "D109",
                    "보험", "D110"
            );

// ✅ 가맹점 정보
            String[] entry = merchants[i % merchants.length];
            dto.setMerchantName(entry[0]);
            dto.setTpbcdNm(entry[1]);
            dto.setTpbcd(tpbcdMap.getOrDefault(entry[1], "D999"));

            // ✅ 금액, 취소 여부
            BigDecimal amount = BigDecimal.valueOf(3000 + (i * 1500 % 25000));
            boolean isCancelled = (i % 6 == 0);

            dto.setAmount(amount);
            dto.setCancelled(isCancelled);
            dto.setCancelAmount(isCancelled ? amount : BigDecimal.ZERO);
            dto.setCancelledAt(isCancelled ? approvedAt : null);

            // ✅ 기타 필드
            dto.setApprovedAt(approvedAt);
            dto.setPaymentDate(paymentDate);
            dto.setAuthNumber("AUTH" + (10000 + i));

            String[] salesTypes = {"1", "2", "3", "6", "7", "8"};
            dto.setSalesType(salesTypes[i % salesTypes.length]); // 순환 방식

            dto.setInstallmentMonth(0);
            dto.setCurrency("KRW");
            dto.setForeignAmount(BigDecimal.ZERO);

            list.add(dto);
        }

        return list;
    }

}


