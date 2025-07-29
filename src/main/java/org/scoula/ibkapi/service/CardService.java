package org.scoula.ibkapi.service;

public interface CardService {

    void syncCardList(Long userId);                     // 카드 목록 조회 및 저장
    void syncCardTransactions(Long userId);             // 카드 승인내역 전체 조회 및 저장
}
