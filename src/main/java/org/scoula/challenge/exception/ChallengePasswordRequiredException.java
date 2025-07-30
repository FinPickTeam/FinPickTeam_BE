package org.scoula.challenge.exception;

import org.scoula.common.exception.BaseException;

public class ChallengePasswordRequiredException extends BaseException {
    public ChallengePasswordRequiredException() {
        super("비밀번호 사용 여부가 true일 때는 비밀번호 입력이 필수입니다.", 400);
    }
}
