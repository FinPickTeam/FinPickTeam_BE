package org.scoula.challenge.exception;

import org.scoula.common.exception.BaseException;

public class ChallengeNotFoundException extends BaseException {
    public ChallengeNotFoundException() {
        super("해당 챌린지를 찾을 수 없습니다.", 404);
    }
}
