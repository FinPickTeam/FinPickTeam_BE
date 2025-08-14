package org.scoula.challenge.rank.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.coin.event.CoinChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeCoinRankUpdateListener {

    private final ChallengeCoinRankUpdateService updateService;

    /**
     * 트랜잭션 커밋 이후 실행: 코인 변경이 월누적에 반영된 경우에만 랭킹 업데이트
     * (동기 처리로도 충분하면 @Async 제거 가능)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCoinChanged(CoinChangedEvent event) {
        if (!event.isMonthlyAffected()) return;
        try {
            updateService.updateUserAndRefreshMonthRanks(event.getUserId());
        } catch (Exception e) {
            log.error("[CoinRankUpdate] failed for userId={}", event.getUserId(), e);
        }
    }
}
