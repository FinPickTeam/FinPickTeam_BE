package org.scoula.nhapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.nhapi.domain.Transaction;
import org.scoula.nhapi.dto.TransactionDto;
import org.scoula.nhapi.mapper.NhAccountMapper;
import org.scoula.nhapi.mapper.NhTransactionMapper;
import org.scoula.nhapi.util.NHApiClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionSyncServiceImpl implements TransactionSyncService {

    private final NHApiClient nhApiClient;
    private final NhAccountMapper nhAccountMapper;
    private final NhTransactionMapper nhtransactionMapper;

    @Override
    public int syncTransaction(String finAccount, String fromDate, String toDate) {
        // 1. 계좌 ID, 유저 ID 조회
        Long accountId = nhAccountMapper.findIdByFinAccount(finAccount);
        Long userId = nhAccountMapper.findUserIdByFinAccount(finAccount);

        // 2. NH API 거래내역 조회
        List<TransactionDto> dtoList = nhApiClient.callTransactionList(finAccount, fromDate, toDate);

        // 3. 각 거래내역 저장
        for (TransactionDto dto : dtoList) {
            dto.setUserId(userId);
            dto.setAccountId(accountId);
            if (dto.getAnalysis() == null) {
                dto.setAnalysis(ruleBased(dto));
            }

            // 4. DTO → domain 변환
            Transaction tx = Transaction.builder()
                    .userId(dto.getUserId())
                    .accountId(dto.getAccountId())
                    .place(dto.getPlace())
                    .date(dto.getDate())
                    .type(dto.getType())
                    .amount(dto.getAmount())
                    .category(dto.getCategory())
                    .memo(dto.getMemo())
                    .analysis(dto.getAnalysis())
                    .build();

            nhtransactionMapper.insertTransaction(tx);
        }

        return dtoList.size();
    }

    private String ruleBased(TransactionDto tx) {
        String place = tx.getPlace();
        if (place.contains("스타벅스")) return "커피 소비";
        if (place.contains("이마트")) return "식비";
        return null;
    }
}
