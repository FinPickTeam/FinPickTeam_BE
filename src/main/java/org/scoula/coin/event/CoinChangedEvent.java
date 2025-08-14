package org.scoula.coin.event;

import lombok.Value;

@Value
public class CoinChangedEvent {
    Long userId;
    boolean monthlyAffected; // true면 monthly_cumulative_amount에 반영된 변경
}
