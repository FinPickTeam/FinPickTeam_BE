package org.scoula.challenge.exception;

import org.scoula.common.exception.BaseException;

public class ChallengeGoalTooSmallException extends BaseException {
    public ChallengeGoalTooSmallException() {
        super("챌린지 목표 금액은 최소 1,000원 이상이어야 합니다.", 400);
    }
}

