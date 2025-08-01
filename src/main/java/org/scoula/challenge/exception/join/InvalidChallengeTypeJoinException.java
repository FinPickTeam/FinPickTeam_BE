package org.scoula.challenge.exception.join;

import org.scoula.common.exception.BaseException;

public class InvalidChallengeTypeJoinException extends BaseException {
    public InvalidChallengeTypeJoinException() {
        super("개인 챌린지에는 참여할 수 없습니다.", 403);
    }
}