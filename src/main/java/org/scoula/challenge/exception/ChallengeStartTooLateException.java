package org.scoula.challenge.exception;

import org.scoula.common.exception.BaseException;

public class ChallengeStartTooLateException extends BaseException {
    public ChallengeStartTooLateException() {
        super("챌린지 시작일은 생성일 기준 일주일 이내여야 합니다.", 400);
    }
}
