package org.scoula.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.scoula.user.domain.UserStatus;

@Mapper
public interface UserStatusMapper {
    void save(UserStatus status);
    boolean isNicknameDuplicated(String nickname);
    String getNickname(Long userId);
}
