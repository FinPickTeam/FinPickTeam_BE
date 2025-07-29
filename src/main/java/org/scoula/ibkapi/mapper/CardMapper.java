package org.scoula.ibkapi.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.scoula.ibkapi.dto.CardDto;
import org.scoula.ibkapi.dto.CardTransactionDto;

import java.util.List;

@Mapper
public interface CardMapper {

    // 카드 목록 저장
    void insertCard(CardDto cardDto);

    // 카드 존재 여부 확인
    CardDto findCardByAltrNo(String oapiCardAltrNo);

    // 승인내역 저장
    void insertCardTransaction(CardTransactionDto transactionDto);

    // 승인내역 중복 확인
    CardTransactionDto findTransactionByAuthNumber(String authNumber);

    // 유저의 카드 목록 조회 (for 프론트)
    List<CardDto> findCardsByUserId(Long userId);

    // 유저의 카드별 거래내역 조회
    List<CardTransactionDto> findTransactionsByCard(String oapiCardAltrNo);
}
