package org.scoula.challenge.exception;

import org.scoula.common.exception.BaseException;

public class ChallengeGoalTooBigException extends BaseException {
    public ChallengeGoalTooBigException() {
        super("챌린지 목표 금액은 10,000,000원 이하이어야 합니다.", 400);
    }
}
