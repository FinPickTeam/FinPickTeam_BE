package org.scoula.challenge.exception;

import org.scoula.common.exception.BaseException;

public class ChallengeDurationTooLongException extends BaseException {
    public ChallengeDurationTooLongException() {
        super("챌린지 기간은 최대 30일까지 가능합니다.", 400);
    }
}
