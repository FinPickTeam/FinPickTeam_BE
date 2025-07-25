package org.scoula.transactions.exception;

import org.scoula.common.exception.BaseException;

public class UserNotFoundException extends BaseException {
    public UserNotFoundException(Long userId) {
        super("해당 유저가 존재하지 않습니다. ID = " + userId, 404);
    }
}
