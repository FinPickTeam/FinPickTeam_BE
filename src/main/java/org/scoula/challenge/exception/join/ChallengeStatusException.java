package org.scoula.challenge.exception.join;

import org.scoula.common.exception.BaseException;

public class ChallengeStatusException extends BaseException {
    public ChallengeStatusException() {
        super("모집 중인 챌린지가 아닙니다.", 400);
    }
}
