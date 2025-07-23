package org.scoula.security.account.mapper;

import org.scoula.user.domain.User;

public interface UserDetailsMapper {
    User get(String email); // 이메일로 유저를 찾음
}
