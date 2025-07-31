package org.scoula.challenge.exception.join;

import org.scoula.common.exception.BaseException;

public class ChallengeLimitExceededException extends BaseException {
    public ChallengeLimitExceededException(String type) {
        super("이미 " + type + " 타입의 챌린지를 3개까지 참여 중입니다.", 400);
    }
}