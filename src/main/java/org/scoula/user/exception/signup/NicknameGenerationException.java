package org.scoula.user.exception.signup;

import org.scoula.common.exception.BaseException;

public class NicknameGenerationException extends BaseException {
    public NicknameGenerationException() {
        super("닉네임 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", 500);
    }
}
