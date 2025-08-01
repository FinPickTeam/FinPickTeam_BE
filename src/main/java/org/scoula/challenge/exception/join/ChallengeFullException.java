package org.scoula.challenge.exception.join;

import org.scoula.common.exception.BaseException;

public class ChallengeFullException extends BaseException {
    public ChallengeFullException() {
        super("해당 챌린지는 참여 인원이 가득 찼습니다.", 409);
    }
}