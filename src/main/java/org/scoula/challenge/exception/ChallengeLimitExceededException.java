package org.scoula.challenge.exception;

import org.scoula.common.exception.BaseException;

public class ChallengeLimitExceededException extends BaseException {
    public ChallengeLimitExceededException(String type) {
        super("이미 " + type + " 챌린지를 최대 참여 중입니다.", 400);
    }
}
