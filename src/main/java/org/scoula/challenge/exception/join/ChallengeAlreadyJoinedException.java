package org.scoula.challenge.exception.join;

import org.scoula.common.exception.BaseException;

public class ChallengeAlreadyJoinedException extends BaseException {
    public ChallengeAlreadyJoinedException() {
        super("이미 챌린지에 참여 중입니다.", 400);
    }
}
