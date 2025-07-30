package org.scoula.challenge.exception;

import org.scoula.common.exception.BaseException;

public class ChallengeDurationTooShortException extends BaseException {
    public ChallengeDurationTooShortException() {
        super("챌린지 기간은 최소 3일 이상이어야 합니다.", 400);
    }
}

