package org.scoula.challenge.enums;

public enum ChallengeStatus {
    RECRUITING, // 모집 중
    CLOSED,     // 모집 마감 (인원 다참)
    IN_PROGRESS, // 시작일 도달 → 자동 업데이트
    COMPLETED   // 종료
}
